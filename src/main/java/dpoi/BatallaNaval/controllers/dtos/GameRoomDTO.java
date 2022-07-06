package dpoi.BatallaNaval.controllers.dtos;

import lombok.*;

import java.util.UUID;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GameRoomDTO {

    private UUID gameRoomId;

    private  String player1Id;

    private String player2Id;

    private String playerToShoot;

    private boolean gameEnded;

}
