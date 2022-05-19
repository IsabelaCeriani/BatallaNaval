package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.controllers.dtos.UserDTO;
import dpoi.BatallaNaval.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("")
    public ResponseEntity<UserDTO> editUser(@RequestBody UserDTO data) {
        return ResponseEntity.ok(userService.addUser(data));
    }
}
