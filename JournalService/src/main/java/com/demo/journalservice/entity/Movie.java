package com.demo.journalservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Movie {

    private int movieId;
    private String title;
    private String genre;
    private byte[] imageData;
    private int releaseYear;

    
}
