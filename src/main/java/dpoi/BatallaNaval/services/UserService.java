package dpoi.BatallaNaval.services;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import dpoi.BatallaNaval.controllers.dtos.UserDTO;
import dpoi.BatallaNaval.exception.NotValidTokenException;
import dpoi.BatallaNaval.model.User;
import dpoi.BatallaNaval.respositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Collections;
import java.util.List;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    public UserDTO getUserInfo(String token) throws GeneralSecurityException, IOException {
        GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(new NetHttpTransport(), new GsonFactory())
                // Specify the CLIENT_ID of the app that accesses the backend:
                .setAudience(Collections.singletonList("471985862015-nm599odp85d7b8lfkhf1nss4m1m1vjtc.apps.googleusercontent.com"))
                .build();

        GoogleIdToken idToken = verifier.verify(token);
        if (idToken != null) {
            GoogleIdToken.Payload payload = idToken.getPayload();

            // Print user identifier
            String userId = payload.getSubject();
            System.out.println("User ID: " + userId);

            // Get profile information from payload
            String email = payload.getEmail();
            String name = (String) payload.get("name");
            String pictureUrl = (String) payload.get("picture");
            String id = (String) payload.get("sub");



            if(userExists(id)){
                return getUser(id).toDTO();

            }else{
                User user = User.builder()
                        .id(id)
                        .email(email)
                        .name(name)
                        .profilePicture(pictureUrl)
                        .gamesPlayed(0)
                        .gamesWon(0)
                        .build();

                return userRepository.save(user).toDTO();
            }



        } else {
            System.out.println("Invalid ID token.");
            throw new NotValidTokenException("Invalid token");
        }
    }

    private boolean userExists(String id) {
        return userRepository.findById(id).isPresent();
    }

    public User getUser(String id){
        return userRepository.findById(id).get();
    }

    public UserDTO getUserDTO(String id){
        return userRepository.findById(id).get().toDTO();
    }


    public void updateWinnerStatics(String winnerId) {
        User user = getUser(winnerId);
        user.setGamesWon(user.getGamesWon() + 1);
        userRepository.save(user);
    }

    public void updatePlayerStatics(String player1Id, String player2Id) {
        User player1 = getUser(player1Id);
        User player2 = getUser(player2Id);
        player1.setGamesPlayed(player1.getGamesPlayed() + 1);
        player2.setGamesPlayed(player2.getGamesPlayed() + 1);
        userRepository.save(player1);
        userRepository.save(player2);
    }

    public List<UserDTO> getUsers() {
        return userRepository.findAll().stream().map(User::toDTO).collect(java.util.stream.Collectors.toList());
    }
}
