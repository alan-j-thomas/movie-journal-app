package com.demo.notification.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.demo.notification.dto.Notification;
import com.demo.notification.service.NotificationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:5173")
public class NotificationController {
	
	private final NotificationService service;
	
	@GetMapping
	@ResponseStatus(code = HttpStatus.OK)
	public List<Notification> getNotifications(){
		return service.getNotifications();
	}
	

	@GetMapping("/user/{userId}")
	@ResponseStatus(HttpStatus.OK)
	public List<Notification> getNotificationsForUser(@PathVariable int userId) {
	    return service.getNotificationsForUser(userId);
	}

}
