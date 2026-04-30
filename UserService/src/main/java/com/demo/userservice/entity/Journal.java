package com.demo.userservice.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Journal {

    private int journalId;
    private int movieId;
    private int userId;
    private String title;
    private String content;
    private String moodTag;
	
    
}
