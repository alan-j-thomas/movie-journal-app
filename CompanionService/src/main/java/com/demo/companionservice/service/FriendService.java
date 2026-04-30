package com.demo.companionservice.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.demo.companionservice.dto.FriendEvent;
import com.demo.companionservice.entity.Friend;
import com.demo.companionservice.entity.Status;
import com.demo.companionservice.exception.RequestNotFoundException;
import com.demo.companionservice.producer.RabbitMQProducer;
import com.demo.companionservice.repository.FriendRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class FriendService {
	
	private final FriendRepository friendRepository;
	
	private final RabbitMQProducer producer;
	
	public Friend sendFriendRequest(int userId, int friendId) {
		
		Friend friend = Friend.builder()
				.userId(userId)
				.friendId(friendId)
				.status(Status.PENDING)
				.build();
		
		//adding the details to event
		FriendEvent event = new FriendEvent();
		event.setFriendId(friendId);
		event.setUserId(userId);
		
		producer.sendMessage(event);
		
		
		return friendRepository.save(friend);
		
	}
	
	public Friend acceptFriendRequest(int requestId) {
		
		Friend friend = friendRepository.findById(requestId)
				.orElseThrow(RequestNotFoundException::new);
		
		friend.setStatus(Status.ACCEPTED);
		
		return friendRepository.save(friend);
	}
	
	public Friend declineFriendRequest(int requestId) {
		
		Friend friend = friendRepository.findById(requestId).orElseThrow(RequestNotFoundException::new);
		
		friend.setStatus(Status.DECLINED);
		
		return friendRepository.save(friend);
	}

	public List<Friend> getFriends(int userId) {
        return friendRepository.findByUserIdOrFriendIdAndStatus(userId, userId, Status.ACCEPTED);
    }

	public List<Friend> getPendingRequests(int userId) {
	    return friendRepository.findByStatusAndUserIdOrFriendId(Status.PENDING, userId, userId);
	}

	public boolean areFriends(int userId1, int userId2) {
    // Implement logic to check if userId1 and userId2 are friends
    // For example, search for an accepted Friend entity between the two users
    return friendRepository.existsByUserIdAndFriendIdAndStatus(userId1, userId2, Status.ACCEPTED) ||
           friendRepository.existsByUserIdAndFriendIdAndStatus(userId2, userId1, Status.ACCEPTED);
}
}
