package dpoi.BatallaNaval.model.messages;

import lombok.*;

import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class RandomShootMessage {

    private UUID gameRoomId;
    private String shooterId;
}
