package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.services.GameRoomService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/gameroom")
public class GameRoomController {

    @Autowired
    GameRoomService gameroomService;

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

}
