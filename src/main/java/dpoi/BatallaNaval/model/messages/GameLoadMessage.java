package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Position;
import dpoi.BatallaNaval.model.Shot;
import dpoi.BatallaNaval.model.Turn;
import lombok.*;
import org.springframework.messaging.MessageHeaders;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class GameLoadMessage implements org.springframework.messaging.Message<GameLoadMessage>{


    private Turn status;
    private  List<Position> positionsPlayer1;
    private  List<Shot> shotsPlayer1;
    private  List<Shot> shotsPlayer2;

    @Override
    public GameLoadMessage getPayload() {
        return this;
    }

    @Override
    public MessageHeaders getHeaders() {
        return null;
    }
}
