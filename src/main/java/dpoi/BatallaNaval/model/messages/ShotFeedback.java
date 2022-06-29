package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Status;
import lombok.*;

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
