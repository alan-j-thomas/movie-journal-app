package com.demo.journalservice.exceptions;

public class MovieNotFoundException extends RuntimeException{

    public MovieNotFoundException() {
        super("Movie Not Found!");
    }

    public MovieNotFoundException(String message) {
        super(message);
    }
}
