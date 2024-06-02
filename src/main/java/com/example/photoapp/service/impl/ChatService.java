package com.example.photoapp.service.impl;

import com.example.photoapp.entity.ChatMessage;
import com.example.photoapp.entity.User;
import com.example.photoapp.entity.dto.ChatMessageDTO;
import com.example.photoapp.repository.ChatMessageRepository;
import com.example.photoapp.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ChatService {

    @Autowired
    private ChatMessageRepository chatMessageRepository;

    @Autowired
    private UserRepository userRepository;

    public List<ChatMessageDTO> getChatMessages(String currentUserEmail, String friendEmail) {
        List<ChatMessage> chatMessages = chatMessageRepository.findBySenderEmailAndRecipientEmailOrRecipientEmailAndSenderEmail(
                currentUserEmail, friendEmail, currentUserEmail, friendEmail);
        for(ChatMessage chatMessage : chatMessages){
            if(chatMessage.getSender().getEmail().equals(currentUserEmail) && (chatMessage.getIsSentToSender() == Boolean.FALSE || chatMessage.getIsSentToSender() == null)){
                chatMessage.setIsSentToSender(Boolean.TRUE);
                chatMessageRepository.save(chatMessage);
            } else if(chatMessage.getRecipient().getEmail().equals(currentUserEmail) && (chatMessage.getIsSentToRecipient() == Boolean.FALSE || chatMessage.getIsSentToRecipient() == null)){
                chatMessage.setIsSentToRecipient(Boolean.TRUE);
                chatMessageRepository.save(chatMessage);
            }
        }

        return chatMessages.stream()
                .map(chatMessage -> {
                    ChatMessageDTO dto = new ChatMessageDTO();
                    dto.setSenderName(chatMessage.getSender().getName());
                    dto.setRecipientName(chatMessage.getRecipient().getName());
                    dto.setText(chatMessage.getText());
                    dto.setTimestamp(chatMessage.getTimestamp());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public List<ChatMessageDTO> getNewChatMessages(String currentUserEmail, String friendEmail) {
        List<ChatMessage> chatMessages = chatMessageRepository.getNewMessage(currentUserEmail, friendEmail);
        chatMessages.forEach(message -> {
            if(message.getSender().getEmail().equals(currentUserEmail) && (message.getIsSentToSender() == Boolean.FALSE || message.getIsSentToSender() == null)){
                message.setIsSentToSender(Boolean.TRUE);
                chatMessageRepository.save(message);
            } else if(message.getRecipient().getEmail().equals(currentUserEmail) && (message.getIsSentToRecipient() == Boolean.FALSE || message.getIsSentToRecipient() == null)){
                message.setIsSentToRecipient(Boolean.TRUE);
                chatMessageRepository.save(message);
            }
        });
        return chatMessages.stream()
                .map(chatMessage -> {
                    ChatMessageDTO dto = new ChatMessageDTO();
                    dto.setSenderName(chatMessage.getSender().getName());
                    dto.setRecipientName(chatMessage.getRecipient().getName());
                    dto.setText(chatMessage.getText());
                    dto.setTimestamp(chatMessage.getTimestamp());
                    return dto;
                })
                .collect(Collectors.toList());
    }

    public void sendMessage(String senderEmail, String recipientEmail, String text) {
        User sender = userRepository.findByEmail(senderEmail);
        User recipient = userRepository.findByEmail(recipientEmail);
        ChatMessage chatMessage = new ChatMessage(sender, recipient, text);
        chatMessageRepository.save(chatMessage);
    }
}