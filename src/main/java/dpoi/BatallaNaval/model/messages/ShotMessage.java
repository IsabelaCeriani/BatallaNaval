package dpoi.BatallaNaval.model.messages;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ShotMessage {

    private UUID gameRoomId;
    private String shooterId;
    private int x;
    private int y;
}
