package com.example.AuthenticationService.controller;

import com.example.AuthenticationService.entity.AuthenticationResponse;
import com.example.AuthenticationService.entity.UserCredentials;
import com.example.AuthenticationService.service.AuthService;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin("*")
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@RequestBody UserCredentials user){
        return ResponseEntity.ok(authService.register(user));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@RequestBody UserCredentials user){
        return ResponseEntity.ok(authService.authenticate(user));
    }
    
//    @GetMapping("/me")
//    public ResponseEntity<?> getUserInfo(@RequestHeader("Authorization") String token) {
//        try {
//            String actualToken = token.replace("Bearer ", "");
//            Claims claims = Jwts.parser()
//                    .setSigningKey("8299d8cf8100ae2abb5a7294136b2247896b549f60f3f59a2139f73154207e56") // Replace with your actual secret key
//                    .parseClaimsJws(actualToken)
//                    .getBody();
//
//            String username = claims.getSubject(); // Assuming subject contains username
//            return ResponseEntity.ok(Collections.singletonMap("username", username));
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Unauthorized: Invalid or expired token");
//        }
//    }

}
