package com.example.photoapp.entity.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
@Getter
@Setter
public class ChatMessageDTO {
    private String senderName;
    private String recipientName;
    private String text;
    private LocalDateTime timestamp;

    // Constructors, getters, and setters
}
