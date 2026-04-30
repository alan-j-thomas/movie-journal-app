package com.demo.userservice.repository;

import com.demo.userservice.entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;



@Repository
public interface UserRepository extends JpaRepository<UserInfo, Integer> {

	UserInfo findByEmail(String email);
	
}
