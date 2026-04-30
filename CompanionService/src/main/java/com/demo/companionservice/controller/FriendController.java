package com.demo.companionservice.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demo.companionservice.entity.Friend;
import com.demo.companionservice.service.FriendService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/friend")
@CrossOrigin(origins = "http://localhost:5173")
@RequiredArgsConstructor
public class FriendController {


	
	private final FriendService friendService;
	
	@PostMapping("/add/{userId}/{friendId}")
	@ResponseStatus(code = HttpStatus.CREATED)
	public Friend sendFriendRequest(@PathVariable int userId,@PathVariable int friendId) {
		
		return friendService.sendFriendRequest(userId, friendId);
		
	}
	
	@PutMapping("/accept/{requestId}")
	@ResponseStatus(code = HttpStatus.OK)
	public Friend acceptRequest(@PathVariable int requestId) {
		return friendService.acceptFriendRequest(requestId);
	}
	
	@PutMapping("/decline/{requestId}")
	@ResponseStatus(code = HttpStatus.OK)
	public Friend declineRequest(@PathVariable int requestId) {
		return friendService.declineFriendRequest(requestId);
		
	}
	
	@GetMapping("/list/{userId}")
	@ResponseStatus(code = HttpStatus.OK)
    public List<Friend> getFriends(@PathVariable int userId) {
        return friendService.getFriends(userId);
    }
	

    @GetMapping("/pending/{userId}")
	@ResponseStatus(code = HttpStatus.OK)
    public List<Friend> getPendingRequests(@PathVariable int userId) {
        return friendService.getPendingRequests(userId);
    }
    
    @GetMapping("/areFriends/{userId1}/{userId2}")
    public boolean areFriends(@PathVariable int userId1,@PathVariable int userId2) {
    	
    	return friendService.areFriends(userId1, userId2);
    }
}
