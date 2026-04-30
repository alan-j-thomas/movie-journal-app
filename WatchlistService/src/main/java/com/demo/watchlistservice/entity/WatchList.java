package com.demo.watchlistservice.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@ToString
public class WatchList {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int watchId;
    private int userId;
    private int movieId;
    
    @NotNull(message = "Status should not be empty")
    private Status status;
    
    @NotEmpty(message = "Note needs to be added")
    private String note;

    @Transient
    private List<Movie> movies = new ArrayList<>();

	
    
    
}
