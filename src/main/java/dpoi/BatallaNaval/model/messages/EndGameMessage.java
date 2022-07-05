package dpoi.BatallaNaval.model.messages;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class EndGameMessage {

    private UUID gameRoomId;
    private String exitedPlayerId;

}
