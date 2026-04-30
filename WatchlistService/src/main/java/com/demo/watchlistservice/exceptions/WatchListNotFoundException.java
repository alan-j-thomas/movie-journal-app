package com.demo.watchlistservice.exceptions;

@SuppressWarnings("serial")
public class WatchListNotFoundException extends RuntimeException{

    public WatchListNotFoundException() {
        super("Watchlist Not Found!");
    }

    public WatchListNotFoundException(String message) {
        super(message);
    }
}
