package dpoi.BatallaNaval.model.messages;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class PositionMessage {

    private UUID gameRoomId;
    private String userId;
    private Integer[][] positions;
}
