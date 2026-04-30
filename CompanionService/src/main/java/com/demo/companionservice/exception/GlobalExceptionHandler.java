package com.demo.companionservice.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
	
	@ExceptionHandler(RequestNotFoundException.class)
	public ResponseEntity<Object> handleExceptions(RequestNotFoundException exception){
		
		return ResponseEntity.status(HttpStatus.NOT_FOUND)
				.body("Request Not Found!!");
	}

}
