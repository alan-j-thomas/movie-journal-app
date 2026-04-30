package com.demo.notification.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.demo.notification.dto.Notification;
import com.demo.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationService {
	
	private final NotificationRepository repository;
	
	public List<Notification> getNotifications() {
		return repository.findAll();
	}
	
	public List<Notification> getNotificationsForUser(int userId) {
	    return repository.findByUserId(userId);
	}

}
