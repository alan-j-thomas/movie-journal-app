package com.demo.chatservice.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WatchlistMovieDTO {
    private int watchId;
    private int userId;
    private int movieId;
    private String status;
    private String note;
    private List<MovieDTO> movies;
}