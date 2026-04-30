package com.demo.journalservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class JournalRequest {

	@NotBlank(message = "Movie Title should not be blank")
    private String movieTitle;
    private int userId;
    
    @NotBlank(message = "Journal title should not be blank")
    private String title;
    
    @NotBlank(message = "Content should not be blank")
    private String content;
    
    @NotBlank(message = "MoodTag should not be blank")
    private String moodTag;
    private int movieId;
	
    
    
    
}
