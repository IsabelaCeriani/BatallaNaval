package dpoi.BatallaNaval.model.messages;

import dpoi.BatallaNaval.model.Status;
import lombok.*;
import org.springframework.messaging.MessageHeaders;

import java.util.Date;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Message implements org.springframework.messaging.Message<Message> {
    private Status status;

    @Override
    public Message getPayload() {
        return this;
    }

    @Override
    public MessageHeaders getHeaders() {
        return null;
    }
}