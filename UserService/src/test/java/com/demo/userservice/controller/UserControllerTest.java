package com.demo.userservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.userservice.dto.UserDTO;
import com.demo.userservice.entity.UserInfo;
import com.demo.userservice.service.UserServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserServiceImpl userService;
    

    @Autowired
    private ObjectMapper objectMapper;

    private UserInfo sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new UserInfo();
        sampleUser.setUserId(1);
        sampleUser.setUserName("Alan J");
        sampleUser.setEmail("alan@example.com");
        sampleUser.setJournals(Collections.emptyList());
        sampleUser.setWatchlists(Collections.emptyList());
    }

    @Test
    void testAddUser() throws Exception {
        when(userService.addUser(any(UserInfo.class))).thenReturn(sampleUser);

        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userName").value("Alan J"));
    }
    
    @Test
    void testGetAllUsersFallback() {
        
        UserController controller = new UserController(null); // null because service isn't used here
        Throwable throwable = new RuntimeException("Simulated failure");

        
        ResponseEntity<List<UserInfo>> response = controller.getAllUsersFallback(throwable);

        
        assertNotNull(response);
        assertEquals(HttpStatusCode.valueOf(200), response.getStatusCode());

        List<UserInfo> users = response.getBody();
        assertNotNull(users);
        assertEquals(1, users.size());

        UserInfo fallbackUser = users.get(0);
        assertEquals(0, fallbackUser.getUserId());
        assertEquals("Dummy", fallbackUser.getUserName());
        assertEquals("dummy@gmail.com", fallbackUser.getEmail());
        assertTrue(fallbackUser.getJournals().isEmpty());
        assertTrue(fallbackUser.getWatchlists().isEmpty());
    }


    @Test
    void testCreateUser() throws Exception {
        UserDTO userDTO = new UserDTO();
        userDTO.setUserName("Piyush");
        userDTO.setEmail("piyush@example.com");

        UserInfo createdUser = new UserInfo();
        createdUser.setUserId(2);
        createdUser.setUserName("Piyush");
        createdUser.setEmail("piyush@example.com");
        createdUser.setJournals(Collections.emptyList());
        createdUser.setWatchlists(Collections.emptyList());

        when(userService.createUser(any(UserDTO.class))).thenReturn(createdUser);

        mockMvc.perform(post("/users/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(userDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userName").value("Piyush"));
    }

    @Test
    void testGetAllUsers() throws Exception {
        when(userService.getAllUsers()).thenReturn(List.of(sampleUser));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userName").value("Alan J"));
    }

    @Test
    void testGetUserById() throws Exception {
        when(userService.getUser(1)).thenReturn(sampleUser);

        mockMvc.perform(get("/users/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("alan@example.com"));
    }

    @Test
    void testDeleteUser() throws Exception {
        when(userService.deleteUser(1)).thenReturn("User deleted");

        mockMvc.perform(delete("/users/1"))
                .andExpect(status().isOk())
                .andExpect(content().string("User deleted"));
    }

    @Test
    void testGetUserIdByEmail() throws Exception {
        when(userService.getUserIdByEmail("alan@example.com")).thenReturn(1);

        mockMvc.perform(get("/users/email/alan@example.com"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }
    
    

    
}