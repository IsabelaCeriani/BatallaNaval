package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Status;
import lombok.*;
import org.springframework.messaging.MessageHeaders;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GameOverMessage {

    private Status status;
    private String winnerId;

}
