package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.controllers.dtos.GameRoomDTO;
import dpoi.BatallaNaval.model.Shot;
import dpoi.BatallaNaval.model.Status;
import dpoi.BatallaNaval.model.Turn;
import dpoi.BatallaNaval.model.messages.*;
import dpoi.BatallaNaval.services.GameRoomService;
import dpoi.BatallaNaval.services.UserService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.messaging.Message;

import java.util.UUID;

@Controller
@RequestMapping("/gameroom")
public class GameRoomController {

    @Autowired
    GameRoomService gameroomService;

    @Autowired
    UserService userService;

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping
    public ResponseEntity<?> createGameRoom(){
        val chatRoomId = gameroomService.createGame();
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomId);
    }

    @GetMapping
    public ResponseEntity<?> getGameRoom(@RequestParam UUID gameRoomId){
        val chatRoomDTO = gameroomService.getGameDTO(gameRoomId);
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomDTO);
    }


    @MessageExceptionHandler()
    @MessageMapping("/join")
    public void joinGameRoom(@Payload JoinMessage message){
        try{
            if(gameIsFull(message.getGameRoomId())){
                if(playerBelongsToGame(message.getGameRoomId(), message.getUserId())){
                    if(gameroomService.gameEnded(message.getGameRoomId())){
                        Message<StatusMessage> statusMessage = MessageBuilder.withPayload(new StatusMessage(Status.GAME_ENDED)).build();
                        simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",statusMessage);
                    }else{
                        loadGame(message.getGameRoomId());
                    }
                }else{
                    Message<StatusMessage> statusMessage = MessageBuilder.withPayload(new StatusMessage(Status.GAME_FULL)).build();
                    simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",statusMessage);
                }
            }else{
                if(playerBelongsToGame(message.getGameRoomId(), message.getUserId())){
                    Message<StatusMessage> statusMessage = MessageBuilder.withPayload(new StatusMessage(Status.WAITING)).build();
                    simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",statusMessage );
                }else{
                    if(gameHasOnePlayer(message.getGameRoomId())){

                        gameroomService.setPlayerTwo(message.getGameRoomId(), message.getUserId());

                        Message<StatusMessage> statusMessage = MessageBuilder.withPayload(new StatusMessage(Status.POSITIONING)).build();
                        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private", statusMessage);
                    }else{

                        gameroomService.setPlayerOne(message.getGameRoomId(), message.getUserId());

                        Message<StatusMessage> statusMessage = MessageBuilder.withPayload(new StatusMessage(Status.WAITING)).build();
                        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",statusMessage );
                    }
                }
            }
        }catch (Exception e){
            System.out.println("ESTE ES EL ERROR :)"+e.getMessage());
            //Message<StatusMessage> statusMessage = MessageBuilder.withPayload(new StatusMessage(Status.ERROR)).build();
            //simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",statusMessage);
        }



    }



    @MessageMapping("/board")
    public void sendPositions(@Payload PositionMessage message){

        if(gameroomService.boardsAreReady(message.getGameRoomId())){
            loadGame(message.getGameRoomId());
        }else{
            gameroomService.setPositions(message.getGameRoomId(), message.getPositions(), message.getUserId());

            Message<StatusMessage> statusMessage = MessageBuilder.withPayload(new StatusMessage(Status.STANDBY)).build();
            simpMessagingTemplate.send("/user/"+message.getUserId()+"/private",statusMessage );
            if(gameroomService.boardsAreReady(message.getGameRoomId())){
                loadGame(message.getGameRoomId());
            }
        }
    }

    @MessageMapping("/shoot")
    public void shoot(@Payload ShotMessage message){
        Shot shot= gameroomService.shoot(message.getGameRoomId(), message.getShooterId(), message.getX(), message.getY());

        //val shotFeedback= new ShotFeedback(shot.getShooterId(),shot.getX(),shot.getY(),shot.isHit());
        Message<ShotFeedback> shotFeedback = MessageBuilder.withPayload(new ShotFeedback(shot.getShooterId(),shot.getX(),shot.getY(),shot.isHit())).build();
        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",shotFeedback);

        GameRoomDTO game= gameroomService.getGameDTO(message.getGameRoomId());

        if(gameroomService.playerWon(message.getGameRoomId(), message.getShooterId())){
            gameroomService.endGame(message.getGameRoomId());
            userService.updateWinnerStatics(message.getShooterId());
            userService.updatePlayerStatics(gameroomService.getGameRoom(message.getGameRoomId()).getPlayer1Id(),gameroomService.getGameRoom(message.getGameRoomId()).getPlayer2Id());
            //val returnMessage= new GameOverMessage(Status.GAME_OVER, message.getShooterId());
            Message<GameOverMessage> gameOverMessage = MessageBuilder.withPayload(new GameOverMessage(Status.GAME_OVER, message.getShooterId())).build();
            simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",gameOverMessage);
        }

        if(gameroomService.isPlayerOne(game.getGameRoomId(),shot.getShooterId())){
            //val messageForPlayer1= new TurnMessage(Turn.OPPONENT_TURN);
            Message<TurnMessage> messageForPlayer1 = MessageBuilder.withPayload(new TurnMessage(Turn.OPPONENT_TURN)).build();
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1);

            //val messageForPlayer2= new TurnMessage(Turn.YOUR_TURN);
            Message<TurnMessage> messageForPlayer2 = MessageBuilder.withPayload(new TurnMessage(Turn.YOUR_TURN)).build();
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2);
        }else{
            Message<TurnMessage> messageForPlayer1 = MessageBuilder.withPayload(new TurnMessage(Turn.YOUR_TURN)).build();
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1);

            Message<TurnMessage> messageForPlayer2 = MessageBuilder.withPayload(new TurnMessage(Turn.OPPONENT_TURN)).build();
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2);
        }
    }


    private void loadGame(UUID gameRoomId) {
        val game = gameroomService.getGameRoom(gameRoomId);
        Message<GameLoadMessage> messageForPlayer1;
        Message<GameLoadMessage> messageForPlayer2;


        if(gameroomService.isPlayerOneTurn(gameRoomId)){
            //messageForPlayer1= new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
            //messageForPlayer2= new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
            messageForPlayer1 = MessageBuilder.withPayload(new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2())).build();
            messageForPlayer2 = MessageBuilder.withPayload(new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1())).build();
        }else{
            //messageForPlayer1= new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
            //messageForPlayer2= new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
            messageForPlayer1 = MessageBuilder.withPayload(new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2())).build();
            messageForPlayer2 = MessageBuilder.withPayload(new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1())).build();

        }

        simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1 );
        simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2 );

        //val returnMessage= new StatusMessage(Status.READY);
        Message<StatusMessage> returnMessage = MessageBuilder.withPayload(new StatusMessage(Status.READY)).build();
        simpMessagingTemplate.convertAndSend("/game/"+ gameRoomId+"/private",returnMessage );
    }

    private boolean playerBelongsToGame(UUID gameRoomId, String userId) {
        return gameroomService.playerBelongsToGame(gameRoomId, userId);
    }

    private boolean gameIsFull(UUID gameRoomID) {
        return gameroomService.gameIsFull(gameRoomID);
    }

    private boolean gameHasOnePlayer(UUID gameRoomId) {
        return gameroomService.hasPlayerOne(gameRoomId);
    }





}
