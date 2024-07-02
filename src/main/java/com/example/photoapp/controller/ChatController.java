package com.example.photoapp.controller;

import com.example.photoapp.entity.ChatMessageRequest;
import com.example.photoapp.entity.Response;
import com.example.photoapp.entity.dto.ChatMessageDTO;
import com.example.photoapp.service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/chat")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @GetMapping("/messages")
    public ResponseEntity<List<ChatMessageDTO>> getChatMessages(@RequestParam String currentUserEmail, @RequestParam String friendEmail) {
        try {
            List<ChatMessageDTO> chatMessages = chatService.getChatMessages(currentUserEmail, friendEmail);
            return ResponseEntity.ok().body(chatMessages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/new-messages")
    public ResponseEntity<List<ChatMessageDTO>> getNewChatMessages(@RequestParam String currentUserEmail, @RequestParam String friendEmail) {
        try {
            List<ChatMessageDTO> chatMessages = chatService.getNewChatMessages(currentUserEmail, friendEmail);
            return ResponseEntity.ok().body(chatMessages);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/send")
    public ResponseEntity<?> sendMessage(@RequestBody ChatMessageRequest messageRequest) {
        chatService.sendMessage(messageRequest.getSenderEmail(), messageRequest.getRecipientEmail(), messageRequest.getText());
        return ResponseEntity.ok(new Response("Message sent successfully"));
    }
}
