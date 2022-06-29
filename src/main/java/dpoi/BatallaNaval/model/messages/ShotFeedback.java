package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Status;
import lombok.*;
import org.springframework.messaging.MessageHeaders;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class ShotFeedback {

    private Status status;
    private String shooterId;
    private int x;
    private int y;
    private boolean hit;

}
