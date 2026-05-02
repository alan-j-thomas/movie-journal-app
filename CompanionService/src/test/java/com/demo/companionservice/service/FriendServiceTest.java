package com.demo.companionservice.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.util.List;
import java.util.Optional;

import com.demo.companionservice.entity.Friend;
import com.demo.companionservice.entity.Status;
import com.demo.companionservice.exception.RequestNotFoundException;
import com.demo.companionservice.producer.RabbitMQProducer;
import com.demo.companionservice.repository.FriendRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;

class FriendServiceTest {

    @Mock
    private FriendRepository friendRepository;

    @Mock
    private RabbitMQProducer producer;

    @InjectMocks
    private FriendService friendService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendFriendRequest() {
        Friend friend = Friend.builder().userId(1).friendId(2).status(Status.PENDING).build();
        when(friendRepository.save(any(Friend.class))).thenReturn(friend);

        Friend result = friendService.sendFriendRequest(1, 2);

        assertEquals(1, result.getUserId());
        assertEquals(2, result.getFriendId());
        assertEquals(Status.PENDING, result.getStatus());
    }

    @Test
    void testAcceptFriendRequest_Success() {
        Friend friend = Friend.builder().status(Status.PENDING).build();
        when(friendRepository.findById(1)).thenReturn(Optional.of(friend));
        when(friendRepository.save(friend)).thenReturn(friend);

        Friend result = friendService.acceptFriendRequest(1);

        assertEquals(Status.ACCEPTED, result.getStatus());
    }

    @Test
    void testAcceptFriendRequest_NotFound() {
        when(friendRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RequestNotFoundException.class, () -> friendService.acceptFriendRequest(1));
    }

    @Test
    void testDeclineFriendRequest_Success() {
        Friend friend = Friend.builder().status(Status.PENDING).build();
        when(friendRepository.findById(1)).thenReturn(Optional.of(friend));
        when(friendRepository.save(friend)).thenReturn(friend);

        Friend result = friendService.declineFriendRequest(1);

        assertEquals(Status.DECLINED, result.getStatus());
    }

    @Test
    void testDeclineFriendRequest_NotFound() {
        when(friendRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(RequestNotFoundException.class, () -> friendService.declineFriendRequest(1));
    }

    @Test
    void testGetFriends() {
        List<Friend> friends = List.of(Friend.builder().userId(1).friendId(2).status(Status.ACCEPTED).build());
        when(friendRepository.findByUserIdOrFriendIdAndStatus(1, 1, Status.ACCEPTED)).thenReturn(friends);

        List<Friend> result = friendService.getFriends(1);

        assertEquals(1, result.size());
        assertEquals(Status.ACCEPTED, result.get(0).getStatus());
    }

    @Test
    void testGetPendingRequests() {
        List<Friend> pending = List.of(Friend.builder().userId(1).friendId(3).status(Status.PENDING).build());
        when(friendRepository.findByStatusAndUserIdOrFriendId(Status.PENDING, 1, 1)).thenReturn(pending);

        List<Friend> result = friendService.getPendingRequests(1);

        assertEquals(1, result.size());
        assertEquals(Status.PENDING, result.get(0).getStatus());
    }

    @Test
    void testAreFriends_True() {
        when(friendRepository.existsByUserIdAndFriendIdAndStatus(1, 2, Status.ACCEPTED)).thenReturn(true);

        boolean result = friendService.areFriends(1, 2);

        assertTrue(result);
    }

    @Test
    void testAreFriends_False() {
        when(friendRepository.existsByUserIdAndFriendIdAndStatus(1, 2, Status.ACCEPTED)).thenReturn(false);
        when(friendRepository.existsByUserIdAndFriendIdAndStatus(2, 1, Status.ACCEPTED)).thenReturn(false);

        boolean result = friendService.areFriends(1, 2);

        assertFalse(result);
    }
}
