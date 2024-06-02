package com.example.photoapp.repository;

import com.example.photoapp.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
    List<ChatMessage> findBySenderEmailAndRecipientEmailOrRecipientEmailAndSenderEmail(
            String senderEmail, String recipientEmail, String recipientEmail2, String senderEmail2);

//    List<ChatMessage> findBySenderEmailAndRecipientEmailAndIsSentOrRecipientEmailAndSenderEmailAndIsSent(
//            String senderEmail, String recipientEmail, Boolean isSent, String recipientEmail2, String senderEmail2, Boolean isSent2);

    @Query("SELECT cm FROM ChatMessage cm WHERE (cm.recipient.email = :emailOne and cm.sender.email = :emailTwo and cm.isSentToRecipient = false) or (cm.sender.email= :emailOne and cm.recipient.email= :emailTwo and cm.isSentToSender = false) ")
    List<ChatMessage> getNewMessage(@Param("emailOne") String emailOne, @Param("emailTwo") String emailTwo);
}
