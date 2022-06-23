package dpoi.BatallaNaval.controllers;

import dpoi.BatallaNaval.model.messages.StatusMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
public class ChatController {

    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;

    @MessageMapping("/message")
    @SendTo("/chatroom/public")
    public StatusMessage receiveMessage(@Payload StatusMessage statusMessage){
        return statusMessage;
    }

    /*@MessageMapping("/private-message")
    public Message recMessage(@Payload Message message){
        //logica para enviar mensaje privado
        //simpMessagingTemplate.convertAndSendToUser(message.getReceiverName(),"/private",message);
        //System.out.println(message.toString());
        //return message;
    }*/
}