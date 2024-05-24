package com.example.photoapp.controller;

import com.example.photoapp.entity.ChatMessage;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.util.HtmlUtils;

@Controller
public class ChatController {
    @GetMapping("/chat")
    public String getChatPage() {
        return "chat"; // This should match the name of your Thymeleaf template (chat.html)
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage send(ChatMessage message) {
        return new ChatMessage(HtmlUtils.htmlEscape(message.getContent()));
    }
}
