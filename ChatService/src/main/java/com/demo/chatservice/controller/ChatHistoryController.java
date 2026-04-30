package com.demo.chatservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demo.chatservice.chat.ChatMessage;
import com.demo.chatservice.repository.ChatMessageRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/chat")
@RequiredArgsConstructor
public class ChatHistoryController {
	
	
	private final ChatMessageRepository chatMessageRepository;
	
	@GetMapping("/messages")
	@ResponseStatus(code = HttpStatus.OK)
	public List<ChatMessage> getAllMessages(){
		
		return chatMessageRepository.findAll();
	}

}
