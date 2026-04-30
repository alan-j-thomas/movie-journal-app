package com.demo.chatservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.chatservice.dto.UserDTO;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

	@GetMapping("/users/{userId}")
    UserDTO getUser(@PathVariable int userId);
	
	@GetMapping("/users/email/{email}")
    int getUserIdByEmail(@PathVariable String email);
}
