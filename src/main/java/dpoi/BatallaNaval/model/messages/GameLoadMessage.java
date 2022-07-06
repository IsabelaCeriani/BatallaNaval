package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Position;
import dpoi.BatallaNaval.model.Shot;
import dpoi.BatallaNaval.model.Turn;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GameLoadMessage{

    private Turn status;
    private  List<Position> positionsPlayer1;
    private  List<Shot> shotsPlayer1;
    private  List<Shot> shotsPlayer2;

}
