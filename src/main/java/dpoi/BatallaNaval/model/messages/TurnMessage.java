package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Turn;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TurnMessage {

    private Turn status;

}
