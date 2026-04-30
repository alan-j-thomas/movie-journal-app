package com.demo.companionservice.exception;

public class RequestNotFoundException extends RuntimeException{

	public RequestNotFoundException() {
		super("Request Not Found!");
	}

	public RequestNotFoundException(String message) {
		super(message);
	}
	
	

}
