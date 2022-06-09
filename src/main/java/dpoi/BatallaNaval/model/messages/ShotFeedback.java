package dpoi.BatallaNaval.model.messages;

import lombok.*;
import org.springframework.messaging.MessageHeaders;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ShotFeedback implements org.springframework.messaging.Message<ShotFeedback>{

    private String shooterId;
    private int x;
    private int y;
    private boolean hit;

    @Override
    public ShotFeedback getPayload() {
        return this;
    }

    @Override
    public MessageHeaders getHeaders() {
        return null;
    }
}
