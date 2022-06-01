package dpoi.BatallaNaval.controllers.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

    private String id;

    private  String name;

    private String email;

    private String profilePicture;

    private int gamesPlayed;

    private int gamesWon;
}
