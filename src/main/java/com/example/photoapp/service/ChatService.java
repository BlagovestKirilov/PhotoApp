package com.example.photoapp.service;

import com.example.photoapp.entity.dto.ChatMessageDTO;

import java.util.List;

public interface ChatService {
    List<ChatMessageDTO> getChatMessages(String currentUserEmail, String friendEmail);
    List<ChatMessageDTO> getNewChatMessages(String currentUserEmail, String friendEmail);
    void sendMessage(String senderEmail, String recipientEmail, String text);
}
