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
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

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


    @MessageMapping("/join")
    public void joinGameRoom(@Payload JoinMessage message){
        if(gameIsFull(message.getGameRoomId())){
            if(playerBelongsToGame(message.getGameRoomId(), message.getUserId())){
                loadGame(message.getGameRoomId());
            }else{
                simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",new Message(Status.GAME_FULL));
            }
        }else{
            if(playerBelongsToGame(message.getGameRoomId(), message.getUserId())){
                val returnMessage= new Message(Status.WAITING);
                simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage );
            }else{
                if(gameHasOnePlayer(message.getGameRoomId())){
                    gameroomService.setPlayerTwo(message.getGameRoomId(), message.getUserId());

                    simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private", new Message(Status.POSITIONING) );
                }else{
                    gameroomService.setPlayerOne(message.getGameRoomId(), message.getUserId());

                    simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",new Message(Status.WAITING) );
                }
            }
        }
    }



    @MessageMapping("/board")
    public void sendPositions(@Payload PositionMessage message){

        if(gameroomService.boardsAreReady(message.getGameRoomId())){
            loadGame(message.getGameRoomId());
        }else{
            gameroomService.setPositions(message.getGameRoomId(), message.getPositions(), message.getUserId());
            val returnMessage= new Message(Status.STANDBY);
            simpMessagingTemplate.send("/user/"+message.getUserId()+"/private",returnMessage );
            if(gameroomService.boardsAreReady(message.getGameRoomId())){
                loadGame(message.getGameRoomId());
            }
        }
    }

    @MessageMapping("/shoot")
    public void shoot(@Payload ShotMessage message){
        Shot shot= gameroomService.shoot(message.getGameRoomId(), message.getShooterId(), message.getX(), message.getY());

        val shotFeedback= new ShotFeedback(shot.getShooterId(),shot.getX(),shot.getY(),shot.isHit());
        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",shotFeedback);

        GameRoomDTO game= gameroomService.getGameDTO(message.getGameRoomId());

        if(gameroomService.playerWon(message.getGameRoomId(), message.getShooterId())){
            userService.updateWinnerStatics(message.getShooterId());
            userService.updatePlayerStatics(gameroomService.getGameRoom(message.getGameRoomId()).getPlayer1Id(),gameroomService.getGameRoom(message.getGameRoomId()).getPlayer2Id());
            val returnMessage= new GameOverMessage(Status.GAME_OVER, message.getShooterId());
            simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage);
        }

        if(gameroomService.isPlayerOne(game.getGameRoomId(),shot.getShooterId())){
            val messageForPlayer1= new TurnMessage(Turn.OPPONENT_TURN);
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1);
            val messageForPlayer2= new TurnMessage(Turn.YOUR_TURN);
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2);
        }else{
            val messageForPlayer1= new TurnMessage(Turn.YOUR_TURN);
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1);
            val messageForPlayer2= new TurnMessage(Turn.OPPONENT_TURN);
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2);
        }
    }


    private void loadGame(UUID gameRoomId) {
        val game = gameroomService.getGameRoom(gameRoomId);
        GameLoadMessage messageForPlayer1;
        GameLoadMessage messageForPlayer2;

        if(gameroomService.isPlayerOneTurn(gameRoomId)){
            messageForPlayer1= new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
            messageForPlayer2= new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
        }else{
             messageForPlayer1= new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
             messageForPlayer2= new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
        }

        simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1 );
        simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2 );

        val returnMessage= new Message(Status.READY);
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
