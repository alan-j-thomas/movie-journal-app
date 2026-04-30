package com.demo.companionservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.demo.companionservice.entity.Friend;
import com.demo.companionservice.entity.Status;

@Repository
public interface FriendRepository extends JpaRepository<Friend, Integer>{

	@Query("SELECT f FROM Friend f WHERE (f.userId = ?1 OR f.friendId = ?2) AND f.status = ?3")
	List<Friend> findByUserIdOrFriendIdAndStatus(int userId, int userId2, Status status);


	List<Friend> findByStatusAndUserIdOrFriendId(Status status, int userId, int friendId);

	
	boolean existsByUserIdAndFriendIdAndStatus(int userId1, int userId2, Status status);

}
