package com.demo.companionservice.controller;

import com.demo.companionservice.entity.Friend;
import com.demo.companionservice.entity.Status;
import com.demo.companionservice.service.FriendService;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FriendController.class)
class FriendControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @SuppressWarnings("removal")
	@MockBean
    private FriendService friendService;

    @Test
    void testSendFriendRequest() throws Exception {
        Friend friend = Friend.builder().userId(1).friendId(2).status(Status.PENDING).build();
        Mockito.when(friendService.sendFriendRequest(1, 2)).thenReturn(friend);

        mockMvc.perform(post("/friend/add/1/2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(1))
                .andExpect(jsonPath("$.friendId").value(2))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void testAcceptRequest() throws Exception {
        Friend friend = Friend.builder().requestId(1).status(Status.ACCEPTED).build();
        Mockito.when(friendService.acceptFriendRequest(1)).thenReturn(friend);

        mockMvc.perform(put("/friend/accept/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACCEPTED"));
    }

    @Test
    void testDeclineRequest() throws Exception {
        Friend friend = Friend.builder().requestId(1).status(Status.DECLINED).build();
        Mockito.when(friendService.declineFriendRequest(1)).thenReturn(friend);

        mockMvc.perform(put("/friend/decline/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DECLINED"));
    }

    @Test
    void testGetFriends() throws Exception {
        List<Friend> friends = List.of(
                Friend.builder().userId(1).friendId(2).status(Status.ACCEPTED).build()
        );
        Mockito.when(friendService.getFriends(1)).thenReturn(friends);

        mockMvc.perform(get("/friend/list/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("ACCEPTED"));
    }

    @Test
    void testGetPendingRequests() throws Exception {
        List<Friend> pending = List.of(
                Friend.builder().userId(1).friendId(3).status(Status.PENDING).build()
        );
        Mockito.when(friendService.getPendingRequests(1)).thenReturn(pending);

        mockMvc.perform(get("/friend/pending/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].status").value("PENDING"));
    }

    @Test
    void testAreFriendsTrue() throws Exception {
        Mockito.when(friendService.areFriends(1, 2)).thenReturn(true);

        mockMvc.perform(get("/friend/areFriends/1/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    void testAreFriendsFalse() throws Exception {
        Mockito.when(friendService.areFriends(1, 2)).thenReturn(false);

        mockMvc.perform(get("/friend/areFriends/1/2"))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }
}
