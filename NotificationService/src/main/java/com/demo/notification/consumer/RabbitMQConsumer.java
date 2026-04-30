package com.demo.notification.consumer;

import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

import com.demo.notification.dto.FriendEvent;
import com.demo.notification.dto.Notification;
import com.demo.notification.dto.UserDto;
import com.demo.notification.external.UserClient;
import com.demo.notification.repository.NotificationRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class RabbitMQConsumer {
	
	private final NotificationRepository notificationRepository;
	
	private final UserClient client;
	@RabbitListener(queues = {"${rabbitmq.queue.name}"})
	public void receiveMessage(FriendEvent event) {
		
		log.info("Event recieved: {}", event.toString());
		
		UserDto user = client.getUser(event.getUserId());
		
        String message = user.getUserName() + " has sent a friend request";
		
		Notification notification = new Notification();
		notification.setFriendId(event.getUserId());
		notification.setUserId(event.getFriendId());
		notification.setMessage(message);
		
		notificationRepository.save(notification);
	}

}
