package com.demo.notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.notification.dto.Notification;


@Repository
public interface NotificationRepository extends JpaRepository<Notification, Integer>{

	
	List<Notification> findByUserId(int userId);

}
