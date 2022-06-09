package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Turn;
import lombok.*;
import org.springframework.messaging.MessageHeaders;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class TurnMessage implements org.springframework.messaging.Message<TurnMessage> {

    private Turn turn;

    @Override
    public TurnMessage getPayload() {
        return this;
    }

    @Override
    public MessageHeaders getHeaders() {
        return null;
    }
}
