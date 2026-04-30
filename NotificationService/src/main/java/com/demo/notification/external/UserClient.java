package com.demo.notification.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.notification.dto.UserDto;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

	@GetMapping("/users/{userId}")
	UserDto getUser(@PathVariable int userId);
}
