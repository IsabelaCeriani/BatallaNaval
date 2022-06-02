package dpoi.BatallaNaval.model;

import dpoi.BatallaNaval.controllers.dtos.GameRoomDTO;
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
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    private String player1Id;

    private String player2Id;


    public GameRoomDTO toDTO() {
        return GameRoomDTO.builder()
                .gameRoomId(this.id)
                .player1Id(this.player1Id)
                .player2Id(this.player2Id)
                .build();
    }
}
