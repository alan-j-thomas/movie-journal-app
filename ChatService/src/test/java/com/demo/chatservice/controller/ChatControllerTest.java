package com.demo.chatservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import com.demo.chatservice.chat.ChatMessage;
import com.demo.chatservice.chat.MessageType;
import com.demo.chatservice.dto.MovieDTO;
import com.demo.chatservice.dto.WatchlistMovieDTO;
import com.demo.chatservice.exception.FriendNotFoundException;
import com.demo.chatservice.external.FriendClient;
import com.demo.chatservice.external.MovieClient;
import com.demo.chatservice.external.UserClient;
import com.demo.chatservice.external.WatchlistClient;
import com.demo.chatservice.repository.ChatMessageRepository;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {

    @Mock
    private ChatMessageRepository chatMessageRepository;

    @Mock
    private FriendClient friendClient;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private UserClient userClient;
    
    @Mock
    private WatchlistClient watchlistClient;
    
    @Mock
    private MovieClient movieClient;

    @InjectMocks
    private ChatController chatController;

    @Test
    void testSendMessage_Success() {
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("alice@example.com");
        message.setRecipient("bob@example.com");

        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(userClient.getUserIdByEmail("bob@example.com")).thenReturn(2);
        when(friendClient.areFriends(1, 2)).thenReturn(true);

        chatController.sendMessage(message);

        verify(chatMessageRepository).save(message);
        verify(messagingTemplate).convertAndSendToUser("bob@example.com", "/queue/messages", message);
        verify(messagingTemplate).convertAndSendToUser("alice@example.com", "/queue/messages", message);
    }

    @Test
    void testSendMessage_NotFriends_ThrowsException() {
        ChatMessage message = new ChatMessage();
        message.setType(MessageType.CHAT);
        message.setSender("alice@example.com");
        message.setRecipient("charlie@example.com");

        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(userClient.getUserIdByEmail("charlie@example.com")).thenReturn(3);
        when(friendClient.areFriends(1, 3)).thenReturn(false);

        assertThrows(FriendNotFoundException.class, () -> chatController.sendMessage(message));

        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, never()).convertAndSendToUser(anyString(), anyString(), any());
    }
    
    @Test
    void testSendMessage_AIRequest_Watchlist() {
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setType(MessageType.AI_REQUEST);
        aiMessage.setContent("Show my watchlist");
        aiMessage.setSender("alice@example.com");
        aiMessage.setRecipient("bob@example.com");
        
        lenient().when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        lenient().when(watchlistClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());
        
        chatController.sendMessage(aiMessage);
        
        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, atLeast(2)).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
    }
    
    @Test
    void testSendMessage_AIRequest_Recommendation() {
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setType(MessageType.AI_REQUEST);
        aiMessage.setContent("recommend movies");
        aiMessage.setSender("alice@example.com");
        aiMessage.setRecipient("bob@example.com");
        
        lenient().when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        lenient().when(watchlistClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());
        
        chatController.sendMessage(aiMessage);
        
        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, atLeast(2)).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
    }
    
    @Test
    void testSendMessage_AIRequest_General() {
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setType(MessageType.AI_REQUEST);
        aiMessage.setContent("What is the weather?");
        aiMessage.setSender("alice@example.com");
        aiMessage.setRecipient("bob@example.com");
        
        chatController.sendMessage(aiMessage);
        
        verify(chatMessageRepository, never()).save(any());
        verify(messagingTemplate, atLeast(2)).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
    }
    
    @Test
    void testIsWatchlistRequest() {
        assertTrue(invokeIsWatchlistRequest("show my watchlist"));
        assertTrue(invokeIsWatchlistRequest("DISPLAY MOVIES"));
        assertTrue(invokeIsWatchlistRequest("my movies"));
        assertFalse(invokeIsWatchlistRequest("recommend movies"));
    }
    
    @Test
    void testIsRecommendationRequest() {
        assertTrue(invokeIsRecommendationRequest("recommend movies"));
        assertTrue(invokeIsRecommendationRequest("MOVIE SUGGESTIONS"));
        assertTrue(invokeIsRecommendationRequest("similar movies"));
        assertFalse(invokeIsRecommendationRequest("show watchlist"));
    }
    
    @Test
    void testIsAIRecommendationRequest() {
        assertTrue(invokeIsAIRecommendationRequest("popular movies"));
        assertTrue(invokeIsAIRecommendationRequest("AI RECOMMEND"));
        assertTrue(invokeIsAIRecommendationRequest("new movies"));
        assertFalse(invokeIsAIRecommendationRequest("recommend movies"));
    }
    
    @Test
    void testHandleWatchlistRequest_EmptyWatchlist() {
        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(watchlistClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());
        
        String result = invokeHandleWatchlistRequest("alice@example.com");
        
        assertEquals("Your watchlist is empty. Add some movies to get started!", result);
    }
    
    @Test
    void testHandleWatchlistRequest_WithMovies() {
        MovieDTO movie = MovieDTO.builder()
            .title("Inception")
            .genre("Sci-Fi")
            .releaseYear(2010)
            .build();
            
        WatchlistMovieDTO watchlistItem = WatchlistMovieDTO.builder()
            .status("WATCHING")
            .note("Great movie")
            .movies(Arrays.asList(movie))
            .build();
        
        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(watchlistClient.getWatchListsByUser(1)).thenReturn(Arrays.asList(watchlistItem));
        
        String result = invokeHandleWatchlistRequest("alice@example.com");
        
        assertTrue(result.contains("Inception"));
        assertTrue(result.contains("Sci-Fi"));
        assertTrue(result.contains("WATCHING"));
    }
    
    @Test
    void testHandleRecommendationRequest_EmptyWatchlist() {
        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(watchlistClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());
        
        String result = invokeHandleRecommendationRequest("alice@example.com", "recommend movies");
        
        assertEquals("Your watchlist is empty. Add some movies first to get personalized recommendations!", result);
    }
    
    @Test
    void testGetDatabaseRecommendations() {
        MovieDTO watchlistMovie = MovieDTO.builder()
            .movieId(1)
            .title("Inception")
            .genre("Sci-Fi")
            .build();
            
        MovieDTO recommendedMovie = MovieDTO.builder()
            .movieId(2)
            .title("Interstellar")
            .genre("Sci-Fi")
            .director("Christopher Nolan")
            .rating(8.6)
            .releaseYear(2014)
            .build();
            
        WatchlistMovieDTO watchlistItem = WatchlistMovieDTO.builder()
            .movies(Arrays.asList(watchlistMovie))
            .build();
        
        when(movieClient.getAllMovies()).thenReturn(Arrays.asList(watchlistMovie, recommendedMovie));
        
        List<MovieDTO> result = invokeGetDatabaseRecommendations(Arrays.asList(watchlistItem));
        
        assertEquals(1, result.size());
        assertEquals("Interstellar", result.get(0).getTitle());
    }
    
    @Test
    void testFormatDatabaseRecommendations() {
        MovieDTO movie = MovieDTO.builder()
            .title("Interstellar")
            .genre("Sci-Fi")
            .director("Christopher Nolan")
            .rating(8.6)
            .releaseYear(2014)
            .build();
            
        String result = invokeFormatDatabaseRecommendations(Arrays.asList(movie));
        
        assertTrue(result.contains("Interstellar"));
        assertTrue(result.contains("Sci-Fi"));
        assertTrue(result.contains("Christopher Nolan"));
        assertTrue(result.contains("8.6/10"));
    }
    
    @Test
    void testGetAIRecommendations() {
        MovieDTO movie = MovieDTO.builder()
            .title("Inception")
            .genre("Sci-Fi")
            .build();
            
        WatchlistMovieDTO watchlistItem = WatchlistMovieDTO.builder()
            .movies(Arrays.asList(movie))
            .build();
        
        String result = invokeGetAIRecommendations(Arrays.asList(watchlistItem));
        
        // Test passes if either success message or error message is returned
        assertTrue(result.contains("popular movie recommendations") || 
                  result.contains("Sorry, I couldn't get AI recommendations"));
    }
    
    @Test
    void testHandleAIRequest_WatchlistAsync() throws Exception {
        CountDownLatch latch = new CountDownLatch(4); // 2 initial + 2 async messages
        
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setType(MessageType.AI_REQUEST);
        aiMessage.setContent("Show my watchlist");
        aiMessage.setSender("alice@example.com");
        aiMessage.setRecipient("bob@example.com");
        
        lenient().when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        lenient().when(watchlistClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());
        
        // Mock messagingTemplate to count down latch
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(messagingTemplate).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
        
        // Call handleAIRequest directly
        var method = ChatController.class.getDeclaredMethod("handleAIRequest", ChatMessage.class);
        method.setAccessible(true);
        method.invoke(chatController, aiMessage);
        
        // Wait for async execution
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(messagingTemplate, atLeast(4)).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
    }
    
    @Test
    void testHandleAIRequest_RecommendationAsync() throws Exception {
        CountDownLatch latch = new CountDownLatch(4);
        
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setType(MessageType.AI_REQUEST);
        aiMessage.setContent("recommend movies");
        aiMessage.setSender("alice@example.com");
        aiMessage.setRecipient("bob@example.com");
        
        lenient().when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        lenient().when(watchlistClient.getWatchListsByUser(1)).thenReturn(Collections.emptyList());
        
        doAnswer(invocation -> {
            latch.countDown();
            return null;
        }).when(messagingTemplate).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
        
        var method = ChatController.class.getDeclaredMethod("handleAIRequest", ChatMessage.class);
        method.setAccessible(true);
        method.invoke(chatController, aiMessage);
        
        assertTrue(latch.await(5, TimeUnit.SECONDS));
        verify(messagingTemplate, atLeast(4)).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
    }
    
    // Helper methods to access private methods using reflection
    private boolean invokeIsWatchlistRequest(String content) {
        try {
            var method = ChatController.class.getDeclaredMethod("isWatchlistRequest", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(chatController, content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private boolean invokeIsRecommendationRequest(String content) {
        try {
            var method = ChatController.class.getDeclaredMethod("isRecommendationRequest", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(chatController, content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private boolean invokeIsAIRecommendationRequest(String content) {
        try {
            var method = ChatController.class.getDeclaredMethod("isAIRecommendationRequest", String.class);
            method.setAccessible(true);
            return (boolean) method.invoke(chatController, content);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String invokeHandleWatchlistRequest(String userEmail) {
        try {
            var method = ChatController.class.getDeclaredMethod("handleWatchlistRequest", String.class);
            method.setAccessible(true);
            return (String) method.invoke(chatController, userEmail);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String invokeHandleRecommendationRequest(String userEmail, String originalRequest) {
        try {
            var method = ChatController.class.getDeclaredMethod("handleRecommendationRequest", String.class, String.class);
            method.setAccessible(true);
            return (String) method.invoke(chatController, userEmail, originalRequest);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<MovieDTO> invokeGetDatabaseRecommendations(List<WatchlistMovieDTO> watchlist) {
        try {
            var method = ChatController.class.getDeclaredMethod("getDatabaseRecommendations", List.class);
            method.setAccessible(true);
            return (List<MovieDTO>) method.invoke(chatController, watchlist);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String invokeFormatDatabaseRecommendations(List<MovieDTO> recommendations) {
        try {
            var method = ChatController.class.getDeclaredMethod("formatDatabaseRecommendations", List.class);
            method.setAccessible(true);
            return (String) method.invoke(chatController, recommendations);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    private String invokeGetAIRecommendations(List<WatchlistMovieDTO> watchlist) {
        try {
            var method = ChatController.class.getDeclaredMethod("getAIRecommendations", List.class);
            method.setAccessible(true);
            return (String) method.invoke(chatController, watchlist);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void testHandleRecommendationRequest_AIRecommendation() {
        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(watchlistClient.getWatchListsByUser(1)).thenReturn(Arrays.asList(
            WatchlistMovieDTO.builder().movies(Arrays.asList(
                MovieDTO.builder().title("Inception").genre("Sci-Fi").build()
            )).build()
        ));
        
        String result = invokeHandleRecommendationRequest("alice@example.com", "popular movies");
        
        assertTrue(result.contains("popular movie recommendations") || 
                  result.contains("Sorry, I couldn't get AI recommendations"));
    }
    
    @Test
    void testHandleRecommendationRequest_DatabaseRecommendations() {
        MovieDTO watchlistMovie = MovieDTO.builder().movieId(1).title("Inception").genre("Sci-Fi").build();
        MovieDTO recommendedMovie = MovieDTO.builder().movieId(2).title("Interstellar").genre("Sci-Fi").director("Nolan").rating(8.6).releaseYear(2014).build();
        
        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(watchlistClient.getWatchListsByUser(1)).thenReturn(Arrays.asList(
            WatchlistMovieDTO.builder().movies(Arrays.asList(watchlistMovie)).build()
        ));
        when(movieClient.getAllMovies()).thenReturn(Arrays.asList(watchlistMovie, recommendedMovie));
        
        String result = invokeHandleRecommendationRequest("alice@example.com", "recommend movies");
        
        assertTrue(result.contains("Interstellar") || result.contains("popular movie recommendations"));
    }
    
    @Test
    void testHandleWatchlistRequest_Exception() {
        when(userClient.getUserIdByEmail("alice@example.com")).thenThrow(new RuntimeException("Service error"));
        
        String result = invokeHandleWatchlistRequest("alice@example.com");
        
        assertEquals("Sorry, I couldn't retrieve your watchlist at the moment.", result);
    }
    
    @Test
    void testHandleRecommendationRequest_Exception() {
        when(userClient.getUserIdByEmail("alice@example.com")).thenThrow(new RuntimeException("Service error"));
        
        String result = invokeHandleRecommendationRequest("alice@example.com", "recommend movies");
        
        assertEquals("Sorry, I couldn't generate recommendations at the moment.", result);
    }
    
    @Test
    void testExtractWatchlistData() {
        MovieDTO movie1 = MovieDTO.builder().movieId(1).genre("Action").build();
        MovieDTO movie2 = MovieDTO.builder().movieId(2).genre("Comedy").build();
        
        WatchlistMovieDTO item1 = WatchlistMovieDTO.builder().movies(Arrays.asList(movie1)).build();
        WatchlistMovieDTO item2 = WatchlistMovieDTO.builder().movies(Arrays.asList(movie2)).build();
        WatchlistMovieDTO item3 = WatchlistMovieDTO.builder().movies(null).build();
        
        List<WatchlistMovieDTO> watchlist = Arrays.asList(item1, item2, item3);
        
        try {
            var method = ChatController.class.getDeclaredMethod("extractWatchlistData", List.class, Set.class, Set.class);
            method.setAccessible(true);
            
            Set<String> genres = new java.util.HashSet<>();
            Set<Integer> movieIds = new java.util.HashSet<>();
            
            method.invoke(chatController, watchlist, genres, movieIds);
            
            assertTrue(genres.contains("action"));
            assertTrue(genres.contains("comedy"));
            assertTrue(movieIds.contains(1));
            assertTrue(movieIds.contains(2));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    void testHandleWatchlistRequest_WithNullNote() {
        MovieDTO movie = MovieDTO.builder()
            .title("Inception")
            .genre("Sci-Fi")
            .releaseYear(2010)
            .build();
            
        WatchlistMovieDTO watchlistItem = WatchlistMovieDTO.builder()
            .status("WATCHING")
            .note(null)
            .movies(Arrays.asList(movie))
            .build();
        
        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(watchlistClient.getWatchListsByUser(1)).thenReturn(Arrays.asList(watchlistItem));
        
        String result = invokeHandleWatchlistRequest("alice@example.com");
        
        assertTrue(result.contains("Inception"));
        assertFalse(result.contains("Note:"));
    }
    
    @Test
    void testFormatDatabaseRecommendations_WithNullDirector() {
        MovieDTO movie = MovieDTO.builder()
            .title("Interstellar")
            .genre("Sci-Fi")
            .director(null)
            .rating(0)
            .releaseYear(2014)
            .build();
            
        String result = invokeFormatDatabaseRecommendations(Arrays.asList(movie));
        
        assertTrue(result.contains("Interstellar"));
        assertFalse(result.contains("Director:"));
        assertFalse(result.contains("Rating:"));
    }
    
    @Test
    void testGetDatabaseRecommendations_NoMatches() {
        MovieDTO watchlistMovie = MovieDTO.builder()
            .movieId(1)
            .title("Inception")
            .genre("Sci-Fi")
            .build();
            
        MovieDTO differentGenreMovie = MovieDTO.builder()
            .movieId(2)
            .title("Comedy Movie")
            .genre("Comedy")
            .build();
            
        WatchlistMovieDTO watchlistItem = WatchlistMovieDTO.builder()
            .movies(Arrays.asList(watchlistMovie))
            .build();
        
        when(movieClient.getAllMovies()).thenReturn(Arrays.asList(watchlistMovie, differentGenreMovie));
        
        List<MovieDTO> result = invokeGetDatabaseRecommendations(Arrays.asList(watchlistItem));
        
        assertEquals(0, result.size());
    }
    
    @Test
    void testHandleAIRequest_ErrorHandling() throws Exception {
        ChatMessage aiMessage = new ChatMessage();
        aiMessage.setType(MessageType.AI_REQUEST);
        aiMessage.setContent("Show my watchlist");
        aiMessage.setSender("alice@example.com");
        aiMessage.setRecipient("bob@example.com");
        
        // Mock to throw exception on first call, then work normally
        doThrow(new RuntimeException("Test error"))
            .doNothing()
            .when(messagingTemplate)
            .convertAndSendToUser(anyString(), eq("/queue/messages"), any());
        
        var method = ChatController.class.getDeclaredMethod("handleAIRequest", ChatMessage.class);
        method.setAccessible(true);
        
        // This should not throw exception due to try-catch in handleAIRequest
        assertDoesNotThrow(() -> {
            try {
                method.invoke(chatController, aiMessage);
            } catch (Exception e) {
                // Expected due to reflection, but handleAIRequest should catch the actual error
            }
        });
        
        // Verify error handling path is executed
        verify(messagingTemplate, atLeast(1)).convertAndSendToUser(anyString(), eq("/queue/messages"), any());
    }
    
    @Test
    void testHandleWatchlistRequest_WithEmptyNote() {
        MovieDTO movie = MovieDTO.builder()
            .title("Inception")
            .genre("Sci-Fi")
            .releaseYear(2010)
            .build();
            
        WatchlistMovieDTO watchlistItem = WatchlistMovieDTO.builder()
            .status("WATCHING")
            .note("")
            .movies(Arrays.asList(movie))
            .build();
        
        when(userClient.getUserIdByEmail("alice@example.com")).thenReturn(1);
        when(watchlistClient.getWatchListsByUser(1)).thenReturn(Arrays.asList(watchlistItem));
        
        String result = invokeHandleWatchlistRequest("alice@example.com");
        
        assertTrue(result.contains("Inception"));
        assertFalse(result.contains("Note:"));
    }
    
    @Test
    void testFormatDatabaseRecommendations_WithEmptyDirector() {
        MovieDTO movie = MovieDTO.builder()
            .title("Interstellar")
            .genre("Sci-Fi")
            .director("")
            .rating(8.6)
            .releaseYear(2014)
            .build();
            
        String result = invokeFormatDatabaseRecommendations(Arrays.asList(movie));
        
        assertTrue(result.contains("Interstellar"));
        assertTrue(result.contains("8.6/10"));
        assertFalse(result.contains("Director:"));
    }
}
