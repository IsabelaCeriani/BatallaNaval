package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.model.GameRoom;
import dpoi.BatallaNaval.model.Status;
import dpoi.BatallaNaval.model.messages.GameLoadMessage;
import dpoi.BatallaNaval.model.messages.JoinMessage;
import dpoi.BatallaNaval.model.messages.Message;
import dpoi.BatallaNaval.model.messages.PositionMessage;
import dpoi.BatallaNaval.services.GameRoomService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
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

    @CrossOrigin(origins = "http://localhost:3000")
    @PostMapping
    public ResponseEntity<?> createGameRoom(){
        val chatRoomId = gameroomService.createGame();
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomId);
    }

    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping
    public ResponseEntity<?> getGameRoom(@RequestParam UUID gameRoomId){
        val chatRoomDTO = gameroomService.getGame(gameRoomId);
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomDTO);
    }

    @MessageMapping("/join")
    public void joinGameRoom(@Payload JoinMessage message){
       if(gameroomService.hasPlayerOne(message.getGameRoomId())){
           gameroomService.joinGame(message.getGameRoomId(), message.getUserId());
           val returnMessage= new Message(Status.POSITIONING);
           simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage );
//           return returnMessage;
       }else{
           gameroomService.setPlayerOne(message.getGameRoomId(), message.getUserId());
           val returnMessage= new Message(Status.WAITING);
           simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage );
//           return returnMessage;
       }
    }

    @MessageMapping("/board")
    public Message sendPositions(@Payload PositionMessage message){
        GameRoom game= gameroomService.setPositions(message.getGameRoomId(), message.getPositions(), message.getUserId());
        if(!game.getPositionsPlayer1().isEmpty()&&!game.getPositionsPlayer2().isEmpty()){
            val returnMessage= new Message(Status.READY);
            simpMessagingTemplate.convertAndSend("/game/"+message.getGameRoomId()+"/private",returnMessage );
            val messageForPlayer1= new GameLoadMessage("GAME_LOAD", game.getPositionsPlayer1(), game.getShotsPlayer1(), game.getShotsPlayer2());
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer1Id()+"/private",messageForPlayer1 );
            val messageForPlayer2= new GameLoadMessage("GAME_LOAD", game.getPositionsPlayer2(), game.getShotsPlayer2(), game.getShotsPlayer1());
            simpMessagingTemplate.convertAndSend("/user/"+game.getPlayer2Id()+"/private",messageForPlayer2 );
            return returnMessage;
        }else{
            val returnMessage= new Message(Status.STANDBY);
            simpMessagingTemplate.convertAndSend("/user/"+message.getUserId()+"/private",returnMessage );
            return returnMessage;
        }
    }


}
