package dpoi.BatallaNaval.model;

import dpoi.BatallaNaval.controllers.dtos.UserDTO;
import lombok.*;

import javax.persistence.*;
import java.util.UUID;

@Entity
@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    private String id;

    private String name;

    private String email;

    private String profilePicture;

    private int gamesPlayed;

    private int gamesWon;


    public UserDTO toDTO() {
        return UserDTO.builder()
                .id(this.id)
                .name(this.name)
                .email(this.email)
                .profilePicture(this.profilePicture)
                .gamesPlayed(this.gamesPlayed)
                .gamesWon(this.gamesWon)
                .build();
    }
}
