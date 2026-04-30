package com.demo.userservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Watchlist {

    private int watchId;
    private int userId;
    private int movieId;
    private Status status;
    private String note;
	
}
