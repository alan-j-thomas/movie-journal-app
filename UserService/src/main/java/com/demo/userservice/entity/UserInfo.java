package com.demo.userservice.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "user_info")
public class UserInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;
    private String userName;
    private String email;
    
    @Enumerated(EnumType.STRING)
    private Role role;
    

    @Transient
    private List<Journal> journals = new ArrayList<>();
    
    @Transient
    private List<Watchlist> watchlists = new ArrayList<>();
    
	
    
    

}
