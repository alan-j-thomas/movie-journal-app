package com.demo.ai_service.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demo.ai_service.model.Message;

@RestController
@RequestMapping("/ai")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8081"})
public class AIController {

	private final ChatClient chatClient;

	public AIController(ChatClient.Builder builder) {
		this.chatClient = builder.build();
	}
	
	
	@PostMapping("/chat")
	@ResponseStatus(code = HttpStatus.OK)
	public String askSomething(@RequestBody Message message) {
		
		return chatClient
				.prompt( message.getPromptMessage() )
				.call()
				.content();
		
	}

}
