package com.demo.chatservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.chatservice.chat.ChatMessage;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Integer>{
	
	List<ChatMessage> findBySenderAndRecipient(String sender, String recipient);
    List<ChatMessage> findBySenderAndRecipientOrRecipientAndSender(String sender, String recipient, String recipient2, String sender2);

}
