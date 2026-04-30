package com.demo.watchlistservice.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;

import com.demo.watchlistservice.dto.WatchlistRequest;
import com.demo.watchlistservice.entity.Status;
import com.demo.watchlistservice.entity.WatchList;
import com.demo.watchlistservice.service.WatchServiceImpl;


@ExtendWith(MockitoExtension.class)
class WatchControllerTest {

    @Mock
    private WatchServiceImpl watchService;

    @InjectMocks
    private WatchController watchController;

    private WatchList sampleWatchList;
    private WatchlistRequest sampleRequest;

    @BeforeEach
    void setup() {
        sampleWatchList = WatchList.builder()
                .watchId(1)
                .userId(100)
                .movieId(1)
                .note("Must watch")
                .status(Status.PLANNED)
                .movies(Collections.emptyList())
                .build();

        sampleRequest = new WatchlistRequest("Inception", 100, Status.PLANNED, "Must watch");
    }
    
    @Test
    void testAddWatchList_Valid() {
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        when(watchService.addWatchList(sampleWatchList)).thenReturn(sampleWatchList);

        ResponseEntity<Object> response = watchController.addWatchList(sampleWatchList, result);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleWatchList, response.getBody());
    }
    
    @Test
    void testAddWatchList_Invalid() {
        BindingResult result = mock(BindingResult.class);
        FieldError fieldError = new FieldError("watchList", "note", "Note is required");

        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(fieldError);

        ResponseEntity<Object> response = watchController.addWatchList(sampleWatchList, result);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Note is required", response.getBody());
    }
    
    @Test
    void testAddToWatchlist_Valid() {
        BindingResult result = mock(BindingResult.class);
        when(result.hasErrors()).thenReturn(false);
        when(watchService.addToWatchList(sampleRequest)).thenReturn(sampleWatchList);

        ResponseEntity<Object> response = watchController.addToWatchlist(sampleRequest, result);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleWatchList, response.getBody());
    }
    
    @Test
    void testAddToWatchlist_Invalid() {
        BindingResult result = mock(BindingResult.class);
        FieldError fieldError = new FieldError("request", "movieTitle", "Movie title is required");

        when(result.hasErrors()).thenReturn(true);
        when(result.getFieldError()).thenReturn(fieldError);

        ResponseEntity<Object> response = watchController.addToWatchlist(sampleRequest, result);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("Movie title is required", response.getBody());
    }
    
    @Test
    void testGetAllWatchLists() {
        when(watchService.getAllWatchLists()).thenReturn(List.of(sampleWatchList));

        ResponseEntity<List<WatchList>> response = watchController.getAllWatchLists();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
    
    @Test
    void testGetSingleWatchList() {
        when(watchService.getSingleWatchList(1)).thenReturn(sampleWatchList);

        ResponseEntity<WatchList> response = watchController.getSingleWatchList(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleWatchList, response.getBody());
    }
    
    @Test
    void testWatchlistFallBack() {
        Throwable throwable = new RuntimeException("Rate limit exceeded");

        ResponseEntity<WatchList> response = watchController.watchlistFallBack(throwable);

        assertEquals(HttpStatus.TOO_MANY_REQUESTS, response.getStatusCode());
        assertEquals(0, response.getBody().getMovieId());
        assertEquals("Dummy", response.getBody().getNote());
    }
    
    @Test
    void testGetWatchListsByUser() {
        when(watchService.getWatchListsByUser(100)).thenReturn(List.of(sampleWatchList));

        ResponseEntity<List<WatchList>> response = watchController.getWatchListsByUser(100);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(1, response.getBody().size());
    }
    
    @Test
    void testDeleteFromWatchList() {
        when(watchService.deleteFromWatchList(1)).thenReturn("Deleted!");

        ResponseEntity<String> response = watchController.deleteFromWatchList(1);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Deleted!", response.getBody());
    }
    
    @Test
    void testUpdateWatchList() {
        when(watchService.updateWatchList(1, sampleWatchList)).thenReturn(sampleWatchList);

        ResponseEntity<WatchList> response = watchController.updateWatchList(1, sampleWatchList);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(sampleWatchList, response.getBody());
    }

    

}
