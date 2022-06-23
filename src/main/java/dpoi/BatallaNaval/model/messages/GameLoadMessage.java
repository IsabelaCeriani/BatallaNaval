package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Position;
import dpoi.BatallaNaval.model.Shot;
import lombok.*;
import lombok.*;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GameLoadMessage {

    private String status;
    private  List<Position> positionsPlayer;
    private  List<Shot> shotsPlayer1;
    private  List<Shot> shotsPlayer2;

}
