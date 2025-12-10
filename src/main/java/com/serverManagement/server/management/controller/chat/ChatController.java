package com.serverManagement.server.management.controller.chat;

import com.serverManagement.server.management.entity.chat.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.web.bind.annotation.RestController;
import java.text.SimpleDateFormat;
import java.util.Date;


@RestController
public class ChatController {
    private static final SimpleDateFormat timeFormat = new SimpleDateFormat("h:mm a");


    @MessageMapping("/chat.sendMessage")
    @SendTo("/topic/public")
    public ChatMessage sendMessage(
            @Payload ChatMessage message
    ) {
        if (message.getSender() == null || message.getSender().trim().isEmpty()) {
            message.setSender("Guest");
        }

        // --- ADD TIMESTAMP FOR LEAVE MESSAGES ---
        if (message.getType() == ChatMessage.MessageType.LEAVE) {
            message.setTimestamp(timeFormat.format(new Date()));
        }


        return message;

    }

    @MessageMapping("/chat.addUser")
    @SendTo("/topic/public")
    public ChatMessage addUser(
            @Payload ChatMessage message,
            SimpMessageHeaderAccessor headerAccessor
    ) {
        if (message.getSender() == null || message.getSender().trim().isEmpty()) {
            message.setSender("Guest");
        }

        // --- ADD TIMESTAMP FOR JOIN MESSAGES ---
        message.setTimestamp(timeFormat.format(new Date()));
        // ---------------------------------------

        if(headerAccessor.getSessionAttributes() != null) {
            headerAccessor.getSessionAttributes().put("username", message.getSender());
        }

        return message;
    }
}
