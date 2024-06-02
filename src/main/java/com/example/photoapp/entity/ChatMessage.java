package com.example.photoapp.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
    @Entity
    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    public class ChatMessage {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        @ManyToOne
        private User sender;

        @ManyToOne
        private User recipient;

        private String text;
        private LocalDateTime timestamp;

        private Boolean isSentToRecipient = Boolean.FALSE;

        private Boolean isSentToSender = Boolean.FALSE;

        public ChatMessage(User sender, User recipient, String text) {
            this.sender = sender;
            this.recipient = recipient;
            this.text = text;
            this.timestamp = LocalDateTime.now();
        }
    }

