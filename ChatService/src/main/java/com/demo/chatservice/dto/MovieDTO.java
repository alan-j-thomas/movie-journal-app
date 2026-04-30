package com.demo.chatservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MovieDTO {
    private int movieId;
    private String title;
    private String genre;
    private String director;
    private String cast;
    private String language;
    private double rating;
    private String summary;
    private int releaseYear;
}