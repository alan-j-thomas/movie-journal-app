package com.demo.watchlistservice.dto;


import com.demo.watchlistservice.entity.Status;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WatchlistRequest {

	@NotBlank(message = "Movie Title should not be blank")
    private String movieTitle;
    private int userId;
    
    @NotNull(message = "Status should not be null")
    private Status status;
    private String note;
    
	
    
}
