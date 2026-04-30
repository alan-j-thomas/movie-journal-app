package com.demo.userservice.service;

import com.demo.userservice.dto.UserDTO;
import com.demo.userservice.entity.Role;
import com.demo.userservice.entity.UserInfo;
import com.demo.userservice.exceptions.UserNotFoundException;
import com.demo.userservice.external.JournalClient;
import com.demo.userservice.external.WatchListClient;
import com.demo.userservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    
    private final UserRepository userRepository;
    
    private final JournalClient journalClient;
    
    private final WatchListClient watchListClient;

    @Override
    public UserInfo addUser(UserInfo userInfo) {
        return userRepository.save(userInfo);
    }

    @Override
    public UserInfo createUser(UserDTO userDTO) {
        UserInfo userInfo = new UserInfo();
        userInfo.setUserName(userDTO.getUserName());
        userInfo.setEmail(userDTO.getEmail());
        userInfo.setRole(Role.valueOf(userDTO.getRole().name()));
        return userRepository.save(userInfo);
    }

    @Override
    public List<UserInfo> getAllUsers() {
        List<UserInfo> allUsers = userRepository.findAll();


        return allUsers.stream().map(userInfo -> {
            userInfo.setJournals(journalClient.getJournalByUser(userInfo.getUserId()));
            userInfo.setWatchlists(watchListClient.getWatchListsByUser(userInfo.getUserId()));

            return userInfo;
        }).toList();
    }

    @Override
    public UserInfo getUser(int userId) {
        UserInfo userInfo = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        userInfo.setJournals(journalClient.getJournalByUser(userInfo.getUserId()));
        userInfo.setWatchlists(watchListClient.getWatchListsByUser(userInfo.getUserId()));

        return userInfo;
    }

    @Override
    public String deleteUser(int userId) {
        UserInfo userInfo = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        
        userRepository.delete(userInfo);
        return "Deleted Successfully";
    }

	@Override
	public int getUserIdByEmail(String email) {
		
		UserInfo user = userRepository.findByEmail(email);
	    if (user == null) throw new UserNotFoundException("User not found for email: " + email);
	    return user.getUserId();
	}
}
