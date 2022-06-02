package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.services.GameRoomService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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

}
