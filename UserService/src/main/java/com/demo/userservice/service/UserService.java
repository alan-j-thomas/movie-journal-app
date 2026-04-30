package com.demo.userservice.service;

import com.demo.userservice.dto.UserDTO;
import com.demo.userservice.entity.UserInfo;

import java.util.List;

public interface UserService {

    UserInfo addUser(UserInfo userInfo);
    UserInfo createUser(UserDTO userDTO);
    List<UserInfo> getAllUsers();
    UserInfo getUser(int userId);
    String deleteUser(int userId);
    
    int getUserIdByEmail(String email);
}
