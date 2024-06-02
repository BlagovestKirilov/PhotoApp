package com.example.photoapp.entity;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ChatMessageRequest {
    private String recipientEmail;
    private String senderEmail;
    private String text;
}
