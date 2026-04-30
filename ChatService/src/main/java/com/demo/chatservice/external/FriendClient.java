package com.demo.chatservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "COMPANION-SERVICE")
public interface FriendClient {

	@GetMapping("/friend/areFriends/{userId1}/{userId2}")
    boolean areFriends(@PathVariable int userId1, @PathVariable int userId2);
}
