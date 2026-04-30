package com.demo.userservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import com.demo.userservice.dto.UserDTO;
import com.demo.userservice.entity.UserInfo;
import com.demo.userservice.exceptions.UserNotFoundException;
import com.demo.userservice.external.JournalClient;
import com.demo.userservice.external.WatchListClient;
import com.demo.userservice.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JournalClient journalClient;

    @Mock
    private WatchListClient watchListClient;

    @InjectMocks
    private UserServiceImpl userService;

    private UserInfo sampleUser;
    
    


    @BeforeEach
    void setUp() {
        sampleUser = new UserInfo();
        sampleUser.setUserId(1);
        sampleUser.setUserName("Ajay");
        sampleUser.setEmail("ajay@example.com");
        sampleUser.setJournals(Collections.emptyList());
        sampleUser.setWatchlists(Collections.emptyList());
    }

    @Test
    void testCreateUser() {
        UserDTO dto = new UserDTO();
        dto.setUserName("Ramesh");
        dto.setEmail("ramesh@example.com");
        dto.setRole(com.demo.userservice.dto.Role.USER);

        UserInfo expectedUser = new UserInfo();
        expectedUser.setUserId(1);
        expectedUser.setUserName("Ajay");
        expectedUser.setEmail("ajay@example.com");
        expectedUser.setRole(com.demo.userservice.entity.Role.USER);

        when(userRepository.save(any(UserInfo.class))).thenReturn(expectedUser);

        UserInfo result = userService.createUser(dto);

        assertNotNull(result);
        assertEquals("Ajay", result.getUserName());
    }
    
    @Test
    void testAddUser() {
        UserInfo newUser = new UserInfo();
        newUser.setUserName("Ajay");
        newUser.setEmail("ajay@example.com");
        newUser.setRole(com.demo.userservice.entity.Role.USER);
        newUser.setJournals(Collections.emptyList());
        newUser.setWatchlists(Collections.emptyList());

        when(userRepository.save(any(UserInfo.class))).thenReturn(newUser);

        UserInfo result = userService.addUser(newUser);

        assertNotNull(result);
        assertEquals("Ajay", result.getUserName());
        assertEquals("ajay@example.com", result.getEmail());
        assertEquals(com.demo.userservice.entity.Role.USER, result.getRole());
    }

    @Test
    void testGetAllUsers() {
        when(userRepository.findAll()).thenReturn(List.of(sampleUser));
        when(journalClient.getJournalByUser(1)).thenReturn(Collections.emptyList());
        when(watchListClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());

        List<UserInfo> users = userService.getAllUsers();

        assertEquals(1, users.size());
        assertEquals("Ajay", users.get(0).getUserName());
    }

    @Test
    void testGetUserIdByEmail_Success() {
        when(userRepository.findByEmail("ajay@example.com")).thenReturn(sampleUser);

        int userId = userService.getUserIdByEmail("ajay@example.com");

        assertEquals(1, userId);
    }
    
    @Test
    void testGetUser_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));
        when(journalClient.getJournalByUser(1)).thenReturn(Collections.emptyList());
        when(watchListClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());

        // Act
        UserInfo result = userService.getUser(1);

        // Assert
        assertNotNull(result);
        assertEquals("Ajay", result.getUserName());
        assertEquals("ajay@example.com", result.getEmail());
        assertTrue(result.getJournals().isEmpty());
        assertTrue(result.getWatchlists().isEmpty());
    }
    
    @Test
    void testGetUser_NotFound() {
        // Arrange
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.getUser(99));
    }

    @Test
    void testDeleteUser_Success() {
        // Arrange
        when(userRepository.findById(1)).thenReturn(Optional.of(sampleUser));

        // Act
        String result = userService.deleteUser(1);

        // Assert
        verify(userRepository).delete(sampleUser);
        assertEquals("Deleted Successfully", result);
    }

    @Test
    void testDeleteUser_NotFound() {
        // Arrange
        when(userRepository.findById(99)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UserNotFoundException.class, () -> userService.deleteUser(99));
    }
}
