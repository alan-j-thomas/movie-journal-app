package com.example.AuthenticationService.repository;

import com.example.AuthenticationService.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserCredentialRepository extends JpaRepository<UserCredentials, Integer> {

    Optional<UserCredentials> findUserByUsername(String username);
}
