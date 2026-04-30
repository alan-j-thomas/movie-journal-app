package com.example.AuthenticationService.service;

import com.example.AuthenticationService.dto.Role;
import com.example.AuthenticationService.dto.UserDTO;
import com.example.AuthenticationService.entity.AuthenticationResponse;
import com.example.AuthenticationService.entity.UserCredentials;
import com.example.AuthenticationService.external.UserClient;
import com.example.AuthenticationService.repository.UserCredentialRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    @Autowired
    private UserCredentialRepository uRepo;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserClient userClient;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private AuthenticationManager authenticationManager;

    public AuthenticationResponse register(UserCredentials request){
        UserCredentials user = new UserCredentials();
        user.setName(request.getName());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());

        user = uRepo.save(user);

        UserDTO userDTO = UserDTO.builder().email(request.getUsername())
        		.userName(request.getName())
        		.role(Role.valueOf(request.getRole().name()))
        		.build();
        
        userClient.createUser(userDTO);

        String token = jwtService.generateToken(user);
        return new AuthenticationResponse(token);
    }

    public AuthenticationResponse authenticate(UserCredentials request){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        //find user from database
        UserCredentials user = uRepo.findUserByUsername(request.getUsername()).orElseThrow();
        String token = jwtService.generateToken(user);    //if authenticated, generate token

        return new AuthenticationResponse(token);
    }
}
