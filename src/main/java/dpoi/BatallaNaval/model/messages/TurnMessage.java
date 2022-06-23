package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Turn;
import lombok.*;
import org.springframework.messaging.MessageHeaders;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TurnMessage {

    private Turn turn;

}
