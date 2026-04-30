package com.demo.journalservice.exceptions;

public class JournalNotFoundException extends RuntimeException{
    public JournalNotFoundException() {
        super("Journal Not Found!");
    }

    public JournalNotFoundException(String message) {
        super(message);
    }
}
