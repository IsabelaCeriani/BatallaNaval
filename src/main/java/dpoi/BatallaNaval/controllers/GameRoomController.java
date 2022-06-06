package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.model.Status;
import dpoi.BatallaNaval.model.messages.JoinMessage;
import dpoi.BatallaNaval.model.messages.Message;
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

    @PostMapping
    public ResponseEntity<?> createGameRoom(@RequestParam String userId){
        val chatRoomId = gameroomService.createGame(userId);
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomId);
    }

    @GetMapping
    public ResponseEntity<?> getGameRoom(@RequestParam UUID gameRoomId){
        val chatRoomDTO = gameroomService.getGame(gameRoomId);
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomDTO);
    }

    @PatchMapping
    public ResponseEntity<?> joinGameRoom(@RequestParam UUID gameRoomId, @RequestParam String userId){
        val chatRoomDTO = gameroomService.joinGame(gameRoomId, userId);
        return ResponseEntity.status(HttpStatus.OK).body(chatRoomDTO);
    }

    @MessageMapping("/join")
    public Message recMessage(@Payload JoinMessage message){
       if(gameroomService.hasPlayerOne(message.getGameRoomId())){
           gameroomService.joinGame(message.getGameRoomId(), message.getUserId());
           val returnMessage= new Message(Status.POSITIONING);
           simpMessagingTemplate.send("/game/"+message.getGameRoomId()+"/private",returnMessage );
           return returnMessage;
       }else{
           gameroomService.setPlayerOne(message.getGameRoomId(), message.getUserId());
           val returnMessage= new Message(Status.WAITING);
           simpMessagingTemplate.send("/game/"+message.getGameRoomId()+"/private",returnMessage );
           return returnMessage;
       }
    }

}
