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

    @GetMapping("/positions")
    public ResponseEntity<?> getPositions(){
        //creat random positions
        val positions = gameroomService.createRandomPositionList();
        return ResponseEntity.status(HttpStatus.OK).body(positions);
    }

    @GetMapping("/getOpponent")
    public ResponseEntity<?> getOpponent(@RequestParam UUID gameRoomId, @RequestParam String userId){
        val opponent = gameroomService.getOpponent(gameRoomId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(opponent);
    }



    @MessageExceptionHandler()
    @MessageMapping("/join")
    public void joinGameRoom(@Payload JoinMessage message){
        try{
            if(gameIsFull(message.getGameRoomId())){
                if(playerBelongsToGame(message.getGameRoomId(), message.getUserId())){
                    if(gameroomService.gameEnded(message.getGameRoomId())){
                        simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",new StatusMessage(Status.GAME_ENDED));
                    }else{
                        loadGame(message.getGameRoomId(), message.getUserId());
                    }
                }else{
                    simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",new StatusMessage(Status.GAME_FULL));
                }
            }else{
                if(playerBelongsToGame(message.getGameRoomId(), message.getUserId())){
                    simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",new StatusMessage(Status.WAITING) );
                }else{
                    if(gameHasOnePlayer(message.getGameRoomId())){

                        gameroomService.setPlayerTwo(message.getGameRoomId(), message.getUserId());

                        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private", new StatusMessage(Status.POSITIONING));
                    }else{

                        gameroomService.setPlayerOne(message.getGameRoomId(), message.getUserId());

                        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",new StatusMessage(Status.WAITING) );
                    }
                }
            }
        }catch (Exception e){
            simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",new StatusMessage(Status.ERROR));
        }

    }



    @MessageMapping("/board")
    public void sendPositions(@Payload PositionMessage message){

        if(gameroomService.boardsAreReady(message.getGameRoomId())){
            loadGame(message.getGameRoomId(), message.getUserId());
        }else{
            gameroomService.setPositions(message.getGameRoomId(), message.getPositions(), message.getUserId());

            simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private", new StatusMessage(Status.STANDBY) );
            if(gameroomService.boardsAreReady(message.getGameRoomId())){
                loadGame(message.getGameRoomId(), message.getUserId());
            }
        }
    }

    @MessageMapping("/randomBoard")
    public void setRandomPositions(@Payload RandomPositionMessage message){

//        if(gameroomService.alreadyHasPositions(message.getGameRoomId(), message.getUserId())){
//            simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private", new StatusMessage(Status.STANDBY) );
//        }else{
            gameroomService.setRandomPositions(message.getGameRoomId(), message.getUserId());

            simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private", new StatusMessage(Status.STANDBY) );

            if(gameroomService.boardsAreReady(message.getGameRoomId())){
                loadGame(message.getGameRoomId(), message.getUserId());
            }
//        }
    }

    @MessageMapping("/chatMessage")
    public void sendMessage(@Payload ChatMessage message){
        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/message",message);
    }

    @MessageMapping("/shoot")
    public void shoot(@Payload ShotMessage message){

        try{
            Shot shot= gameroomService.shoot(message.getGameRoomId(), message.getShooterId(), message.getX(), message.getY());
            sendShotFeedback(message.getGameRoomId(),message.getShooterId(), shot);

        }catch(Exception e){
            simpMessagingTemplate.convertAndSend("/user/"+message.getShooterId()+"/private",new TurnMessage(Turn.YOUR_TURN));
        }

    }

    @MessageMapping("/randomShoot")
    public void randomShoot(@Payload RandomShootMessage message){
        Shot shot= gameroomService.shootRandom(message.getGameRoomId(), message.getShooterId());
        sendShotFeedback(message.getGameRoomId(),message.getShooterId(), shot);
    }

    @MessageMapping("/endGame")
    public void endGame(@Payload EndGameMessage message){
        gameroomService.abandonGame(message.getGameRoomId(), message.getExitedPlayerId());
        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private", new StatusMessage(Status.GAME_ENDED));
    }

    private void sendShotFeedback(UUID gameRoomId, String shooterId, Shot shot) {
        simpMessagingTemplate.convertAndSend("/game/"+gameRoomId+"/private",new ShotFeedback(Status.FEEDBACK,shot.getShooterId(),shot.getX(),shot.getY(),shot.isHit()));

        GameRoomDTO game= gameroomService.getGameDTO(gameRoomId);

        if(gameroomService.playerWon(gameRoomId, shooterId)){
            userService.updateWinnerStatics(shooterId);
            userService.updatePlayerStatics(gameroomService.getGameRoom(gameRoomId).getPlayer1Id(),gameroomService.getGameRoom(gameRoomId).getPlayer2Id());
            gameroomService.endGame(gameRoomId);
            simpMessagingTemplate.convertAndSend("/game/"+gameRoomId+"/private",new GameOverMessage(Status.GAME_OVER, shooterId));
            return;
        }

        sendTurns(shot, game);
    }

    private void sendTurns(Shot shot, GameRoomDTO game) {
        if(gameroomService.isPlayerOne(game.getGameRoomId(),shot.getShooterId())){
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",new TurnMessage(Turn.OPPONENT_TURN));

            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",new TurnMessage(Turn.YOUR_TURN));
        }else{
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",new TurnMessage(Turn.YOUR_TURN));

            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",new TurnMessage(Turn.OPPONENT_TURN));
        }
    }


    private void loadGame(UUID gameRoomId, String userId) {
        val game = gameroomService.getGameRoom(gameRoomId);

        if(gameroomService.allPositionsSet(gameRoomId)){
            GameLoadMessage messageForPlayer1;
            GameLoadMessage messageForPlayer2;


            if(gameroomService.isPlayerOneTurn(gameRoomId)){
                messageForPlayer1 = new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
                messageForPlayer2 = new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
            }else{
                messageForPlayer1= new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
                messageForPlayer2= new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
            }

            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1 );
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2 );

//            simpMessagingTemplate.convertAndSend("/game/"+ gameRoomId+"/private",new StatusMessage(Status.READY) );
        }else{
            if(userId.equals(game.getPlayer1Id())){
                if(game.getPositionsPlayer1().isEmpty()){
                    simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",new StatusMessage(Status.POSITIONING) );
                }else{
                    simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",new StatusMessage(Status.STANDBY) );
                }
            }else{
                if(game.getPositionsPlayer2().isEmpty()){
                    simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",new StatusMessage(Status.POSITIONING) );
                }else{
                    simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",new StatusMessage(Status.STANDBY) );
                }
            }
        }
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
