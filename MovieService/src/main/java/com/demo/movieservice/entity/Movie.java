package com.demo.movieservice.entity;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Entity
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "movie")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int movieId;
    
    @NotBlank(message = "Movie title should not be blank")
    private String title;
    
    @NotBlank(message = "Genre should not be blank")
    private String genre;
    
    private String director;
    private String cast;
    private String language;
    private double rating;
    
    @Column(columnDefinition = "LONGTEXT")
    private String summary;
    
    
    private String imageName;
    private String imageType;
    
    @Lob
    private byte[] imageData;
    
    private int releaseYear;
    
	
    
    
}
