package com.example.AuthenticationService.external;

import com.example.AuthenticationService.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "USER-SERVICE")
public interface UserClient {

    @PostMapping("/users/add")
    void createUser(@RequestBody UserDTO userDto);
}
