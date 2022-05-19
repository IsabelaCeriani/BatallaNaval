package dpoi.BatallaNaval.services;

import dpoi.BatallaNaval.controllers.dtos.UserDTO;
import dpoi.BatallaNaval.model.User;
import dpoi.BatallaNaval.respositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public UserDTO addUser(UserDTO data) {
        User user = new User(data.getName(), data.getPassword());
        userRepository.save(user);
        return data;
    }
}
