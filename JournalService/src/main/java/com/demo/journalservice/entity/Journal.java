package com.demo.journalservice.entity;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class Journal {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int journalId;
    private int movieId;
    private int userId;
    
    @NotBlank(message = "Journal title should not be blank")
    private String title;
    
    @NotBlank(message = "Content should not be blank")
    private String content;
    
    @NotBlank(message = "MoodTag should not be blank")
    private String moodTag;

    @Transient
    private List<Movie> movies = new ArrayList<>();

	
    
}
