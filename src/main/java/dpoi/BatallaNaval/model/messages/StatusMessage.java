package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Status;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class StatusMessage {
    private Status status;

}