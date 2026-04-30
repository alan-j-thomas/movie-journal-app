package com.demo.chatservice.exception;

public class FriendNotFoundException extends RuntimeException{

	public FriendNotFoundException() {
		super("Friend Not Found!");
	}

	public FriendNotFoundException(String message) {
		super(message);
	}

	
}
