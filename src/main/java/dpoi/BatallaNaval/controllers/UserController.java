package dpoi.BatallaNaval.controllers;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dpoi.BatallaNaval.controllers.dtos.UserDTO;
import dpoi.BatallaNaval.services.UserService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.security.GeneralSecurityException;
import java.util.List;

@Controller
@RequestMapping("/player")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("")
    public ResponseEntity<UserDTO> addUser(@RequestParam String token) throws GeneralSecurityException, IOException {
        val user = userService.getUserInfo(token);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }

    @GetMapping("")
    public ResponseEntity<List<UserDTO>> getUsers() {
        val users = userService.getUsers();
        return ResponseEntity.status(HttpStatus.OK).body(users);
    }

    //get user by id
    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> getUser(@PathVariable String id) {
        val user = userService.getUserDTO(id);
        return ResponseEntity.status(HttpStatus.OK).body(user);
    }


}
