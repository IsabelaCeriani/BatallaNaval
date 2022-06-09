package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.controllers.dtos.GameRoomDTO;
import dpoi.BatallaNaval.model.GameRoom;
import dpoi.BatallaNaval.model.Shot;
import dpoi.BatallaNaval.model.Status;
import dpoi.BatallaNaval.model.Turn;
import dpoi.BatallaNaval.model.messages.*;
import dpoi.BatallaNaval.services.GameRoomService;
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
    private SimpMessagingTemplate simpMessagingTemplate;

    @PostMapping
    public ResponseEntity<?> createGameRoom(){
        val chatRoomId = gameroomService.createGame();
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomId);
    }

    @GetMapping
    public ResponseEntity<?> getGameRoom(@RequestParam UUID gameRoomId){
        val chatRoomDTO = gameroomService.getGame(gameRoomId);
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomDTO);
    }


    @MessageMapping("/join")
    public Message joinGameRoom(@Payload JoinMessage message){
       if(gameroomService.hasPlayerOne(message.getGameRoomId())){
           gameroomService.joinGame(message.getGameRoomId(), message.getUserId());
           val returnMessage= new Message(Status.POSITIONING);
           simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage );
           return returnMessage;
       }else{
           gameroomService.setPlayerOne(message.getGameRoomId(), message.getUserId());
           val returnMessage= new Message(Status.WAITING);
           simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage );
           return returnMessage;
       }
    }

    @MessageMapping("/board")
    public Message sendPositions(@Payload PositionMessage message){
        GameRoom game= gameroomService.setPositions(message.getGameRoomId(), message.getPositions(), message.getUserId());
        if(!game.getPositionsPlayer1().isEmpty()&&!game.getPositionsPlayer2().isEmpty()){
            val messageForPlayer1= new GameLoadMessage(Turn.YOUR_TURN,game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1 );
            val messageForPlayer2= new GameLoadMessage(Turn.OPPONENT_TURN,game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2 );
            val returnMessage= new Message(Status.READY);
            simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage );
            return returnMessage;
        }else{
            val returnMessage= new Message(Status.STANDBY);
            simpMessagingTemplate.send("/user/"+message.getUserId()+"/private",returnMessage );
            return returnMessage;
        }
    }

    @MessageMapping("/shoot")
    public void shoot(@Payload ShotMessage message){
        Shot shot= gameroomService.shoot(message.getGameRoomId(), message.getShooterId(), message.getX(), message.getY());

        val shotFeedback= new ShotFeedback(shot.getShooterId(),shot.getX(),shot.getY(),shot.isHit());
        simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",shotFeedback);

        GameRoomDTO game= gameroomService.getGame(message.getGameRoomId());

        if(gameroomService.ifPlayerWon(message.getGameRoomId(), message.getShooterId())){
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


}
