package com.demo.watchlistservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import com.demo.watchlistservice.dto.WatchlistRequest;
import com.demo.watchlistservice.entity.Movie;
import com.demo.watchlistservice.entity.Status;
import com.demo.watchlistservice.entity.WatchList;
import com.demo.watchlistservice.exceptions.MovieNotFoundException;
import com.demo.watchlistservice.exceptions.WatchListNotFoundException;
import com.demo.watchlistservice.external.MovieClient;
import com.demo.watchlistservice.repository.WatchRepository;
@ExtendWith(MockitoExtension.class)
class WatchServiceImplTest {

    @Mock
    private WatchRepository watchRepository;

    @Mock
    private MovieClient movieClient;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private WatchServiceImpl watchService;

    private WatchList sampleWatchList;
    private Movie sampleMovie;

    @BeforeEach
    void setup() {
        sampleMovie = new Movie(1, "Inception", "Sci-Fi", 2010);
        sampleWatchList = WatchList.builder()
                .watchId(1)
                .userId(100)
                .movieId(1)
                .note("Must watch")
                .status(Status.PLANNED)
                .movies(List.of(sampleMovie))
                .build();
    }
    
    @Test
    void testAddWatchList() {
        when(watchRepository.save(sampleWatchList)).thenReturn(sampleWatchList);

        WatchList result = watchService.addWatchList(sampleWatchList);

        assertEquals(sampleWatchList, result);
        verify(watchRepository).save(sampleWatchList);
    }
    
    @Test
    void testGetAllWatchLists() {
        when(watchRepository.findAll()).thenReturn(List.of(sampleWatchList));
        when(movieClient.getMovies(1)).thenReturn(sampleMovie);

        List<WatchList> result = watchService.getAllWatchLists();

        assertEquals(1, result.size());
        assertEquals(sampleMovie, result.get(0).getMovies().get(0));
    }
    
    @Test
    void testGetSingleWatchList_Success() {
        when(watchRepository.findById(1)).thenReturn(Optional.of(sampleWatchList));
        when(movieClient.getMovies(1)).thenReturn(sampleMovie);

        WatchList result = watchService.getSingleWatchList(1);

        assertEquals(sampleMovie, result.getMovies().get(0));
    }
    
    @Test
    void testGetSingleWatchList_NotFound() {
        when(watchRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(WatchListNotFoundException.class, () -> watchService.getSingleWatchList(1));
    }
    
    @Test
    void testGetWatchListsByUser() {
        when(watchRepository.getWatchListsByUserId(100)).thenReturn(List.of(sampleWatchList));
        when(movieClient.getMovies(1)).thenReturn(sampleMovie);

        List<WatchList> result = watchService.getWatchListsByUser(100);

        assertEquals(1, result.size());
        assertEquals(sampleMovie, result.get(0).getMovies().get(0));
    }
    
    @Test
    void testAddToWatchList_Success() {
        WatchlistRequest request = new WatchlistRequest("Inception", 100, Status.PLANNED, "Must watch");

        when(restTemplate.getForObject(anyString(), eq(Movie.class))).thenReturn(sampleMovie);
        when(watchRepository.save(any(WatchList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WatchList result = watchService.addToWatchList(request);

        assertEquals(100, result.getUserId());
        assertEquals(1, result.getMovieId());
    }
    
    @Test
    void testAddToWatchList_MovieNotFound() {
        WatchlistRequest request = new WatchlistRequest("Unknown", 100, Status.PLANNED, "Nope");

        when(restTemplate.getForObject(anyString(), eq(Movie.class))).thenReturn(null);

        assertThrows(MovieNotFoundException.class, () -> watchService.addToWatchList(request));
    }
    
    @Test
    void testDeleteFromWatchList_Success() {
        when(watchRepository.findById(1)).thenReturn(Optional.of(sampleWatchList));

        String result = watchService.deleteFromWatchList(1);

        assertEquals("Deleted!", result);
        verify(watchRepository).delete(sampleWatchList);
    }
    
    @Test
    void testDeleteFromWatchList_NotFound() {
        when(watchRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(WatchListNotFoundException.class, () -> watchService.deleteFromWatchList(1));
    }
    
    @Test
    void testUpdateWatchList_Success() {
        WatchList updated = WatchList.builder()
                .status(Status.COMPLETED)
                .note("Great movie")
                .build();

        when(watchRepository.findById(1)).thenReturn(Optional.of(sampleWatchList));
        when(watchRepository.save(any(WatchList.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WatchList result = watchService.updateWatchList(1, updated);

        assertEquals(Status.COMPLETED, result.getStatus());
        assertEquals("Great movie", result.getNote());
    }
    
    @Test
    void testUpdateWatchList_NotFound() {
        WatchList updated = new WatchList();
        when(watchRepository.findById(1)).thenReturn(Optional.empty());

        assertThrows(WatchListNotFoundException.class, () -> watchService.updateWatchList(1, updated));
    }
    

}
