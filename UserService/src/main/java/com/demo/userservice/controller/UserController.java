package com.demo.userservice.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.demo.userservice.dto.UserDTO;
import com.demo.userservice.entity.UserInfo;
import com.demo.userservice.service.UserServiceImpl;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserController {

    
    private final UserServiceImpl userService;

    @PostMapping
    public ResponseEntity<UserInfo> addUser(@RequestBody UserInfo userInfo){
        return ResponseEntity.ok(userService.addUser(userInfo));
    }

    @PostMapping("/add")
    public ResponseEntity<UserInfo> createUser(@RequestBody UserDTO userDto){
        UserInfo user = userService.createUser(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }


    @GetMapping
    @CircuitBreaker(name = "usersCircuitBreaker", fallbackMethod = "getAllUsersFallback")
    public ResponseEntity<List<UserInfo>> getAllUsers(){
        return ResponseEntity.ok(userService.getAllUsers());
    }
    

    public ResponseEntity<List<UserInfo>> getAllUsersFallback(Throwable throwable){
    	UserInfo user = new UserInfo();
    	user.setUserId(0);
    	user.setUserName("Dummy");
    	user.setEmail("dummy@gmail.com");
    	user.setJournals(Collections.emptyList());
    	user.setWatchlists(Collections.emptyList());
    	
    	log.warn("Fallback triggered due to: {}", throwable.getMessage());

    	return ResponseEntity.ok(List.of(user));

    }


    @GetMapping("/{userId}")
    public ResponseEntity<UserInfo> getUser(@PathVariable int userId){
        return ResponseEntity.ok(userService.getUser(userId));
    }

    @DeleteMapping("/{userId}")
    public ResponseEntity<String> deleteUser(@PathVariable int userId){
        return ResponseEntity.ok(userService.deleteUser(userId));
    }
    
    @GetMapping("/email/{email}")
    public ResponseEntity<Integer> getUserId(@PathVariable String email){
    	return ResponseEntity.ok(userService.getUserIdByEmail(email));
    }
}
