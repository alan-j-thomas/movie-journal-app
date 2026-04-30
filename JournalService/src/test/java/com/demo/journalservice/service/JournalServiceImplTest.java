package com.demo.journalservice.service;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.client.RestTemplate;

import com.demo.journalservice.dto.JournalRequest;
import com.demo.journalservice.entity.Journal;
import com.demo.journalservice.entity.Movie;
import com.demo.journalservice.exceptions.JournalNotFoundException;
import com.demo.journalservice.exceptions.MovieNotFoundException;
import com.demo.journalservice.external.MovieClient;
import com.demo.journalservice.repository.JournalRepository;

import feign.FeignException;
import feign.Request;

class JournalServiceImplTest {

	@InjectMocks
    private JournalServiceImpl journalService;

    @Mock
    private JournalRepository journalRepository;

    @Mock
    private MovieClient movieClient;

    @Mock
    private RestTemplate restTemplate;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }
    
    private Journal mockJournal() {
        Journal journal = new Journal();
        journal.setJournalId(1);
        journal.setUserId(100);
        journal.setMovieId(200);
        journal.setTitle("My Journal");
        journal.setContent("Some thoughts");
        journal.setMoodTag("Happy");
        return journal;
    }

    private Movie mockMovie() {
        Movie movie = new Movie();
        movie.setMovieId(200);
        movie.setTitle("Inception");
        return movie;
    }
    
    @Test
    void testAddJournal() {
        Journal journal = mockJournal();
        when(journalRepository.save(journal)).thenReturn(journal);

        Journal result = journalService.addJournal(journal);
        assertEquals(journal.getJournalId(), result.getJournalId());
        verify(journalRepository).save(journal);
    }
    
    @Test
    void testAddToJournal_ValidMovie() {
        JournalRequest request = new JournalRequest("Inception", 100, "Excited", "Title", "Content", 200);
        Movie movie = mockMovie();

        when(restTemplate.getForObject(anyString(), eq(Movie.class))).thenReturn(movie);
        when(journalRepository.save(any(Journal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Journal result = journalService.addToJournal(request);
        assertEquals(100, result.getUserId());
        assertEquals(200, result.getMovieId());
    }
    
    @Test
    void testAddToJournal_InvalidMovie() {
        JournalRequest request = new JournalRequest("Unknown", 100, "Sad", "Title", "Content", 200);

        when(restTemplate.getForObject(anyString(), eq(Movie.class))).thenReturn(null);

        assertThrows(MovieNotFoundException.class, () -> journalService.addToJournal(request));
    }
    
    @Test
    void testGetAllJournals() {
        Journal journal = mockJournal();
        when(journalRepository.findAll()).thenReturn(List.of(journal));
        when(movieClient.getMovies(200)).thenReturn(mockMovie());

        List<Journal> result = journalService.getAllJournals();
        assertEquals(1, result.size());
        assertEquals(200, result.get(0).getMovieId());
    }
    
    @Test
    void testGetJournal_ValidId() {
        Journal journal = mockJournal();
        when(journalRepository.findById(1)).thenReturn(Optional.of(journal));
        when(movieClient.getMovies(200)).thenReturn(mockMovie());

        Journal result = journalService.getJournal(1);
        assertEquals(1, result.getJournalId());
    }
    
    @Test
    void testGetJournal_InvalidId() {
        when(journalRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(JournalNotFoundException.class, () -> journalService.getJournal(99));
    }
    
    @Test
    void testGetJournalsByUser() {
        Journal journal = mockJournal();
        when(journalRepository.getJournalsByUserId(100)).thenReturn(List.of(journal));
        when(movieClient.getMovies(200)).thenReturn(mockMovie());

        List<Journal> result = journalService.getJournalsByUser(100);
        assertEquals(1, result.size());
        assertEquals(100, result.get(0).getUserId());
    }
    
    @Test
    void testUpdateJournal_Valid() {
        JournalRequest request = new JournalRequest("Inception", 100, "Updated Title", "Updated Content","Joyful", 200);
        Journal journal = mockJournal();

        when(journalRepository.findById(1)).thenReturn(Optional.of(journal));
        when(journalRepository.save(any(Journal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Journal result = journalService.updateJournal(1, request);
        assertEquals("Updated Title", result.getTitle());
    }
    
    @Test
    void testUpdateJournal_InvalidId() {
        JournalRequest request = new JournalRequest("Inception", 100, "Updated Title", "Updated Content","Joyful", 200);
        when(journalRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(JournalNotFoundException.class, () -> journalService.updateJournal(99, request));
    }
    
    @Test
    void testDeleteJournal_Valid() {
        Journal journal = mockJournal();
        when(journalRepository.findById(1)).thenReturn(Optional.of(journal));

        String result = journalService.deleteJournal(1);
        assertEquals("Journal deleted Successfully!", result);
        verify(journalRepository).delete(journal);
    }
    
    @Test
    void testDeleteJournal_InvalidId() {
        when(journalRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(JournalNotFoundException.class, () -> journalService.deleteJournal(99));
    }
    
    
    @Test
    void testGetAllJournals_FeignExceptionHandled() {
        // Arrange: Create a journal with a valid movieId
        Journal journal = new Journal();
        journal.setJournalId(1);
        journal.setUserId(100);
        journal.setMovieId(200);
        journal.setTitle("Test Title");
        journal.setContent("Test Content");
        journal.setMoodTag("Happy");

        // Simulate FeignException when fetching movie
        FeignException feignException = new FeignException.NotFound(
            "Movie not found",
            Request.create(Request.HttpMethod.GET, "/movies/200", java.util.Collections.emptyMap(), null, null, null),
            null,
            null
        );

        when(journalRepository.findAll()).thenReturn(List.of(journal));
        when(movieClient.getMovies(200)).thenThrow(feignException);

        // Act
        List<Journal> result = journalService.getAllJournals();

        // Assert
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getMovies().size()); // Should be empty due to fallback
    }
    

    @Test
    void testGetJournalsByUser_FeignExceptionHandled() {
        // Arrange: Create a journal with a valid movieId
        Journal journal = new Journal();
        journal.setJournalId(1);
        journal.setUserId(100);
        journal.setMovieId(200);
        journal.setTitle("Test Title");
        journal.setContent("Test Content");
        journal.setMoodTag("Happy");

        // Simulate FeignException when fetching movie
        FeignException feignException = new FeignException.NotFound(
            "Movie not found",
            Request.create(Request.HttpMethod.GET, "/movies/200", java.util.Collections.emptyMap(), null, null, null),
            null,
            null
        );

        when(journalRepository.getJournalsByUserId(100)).thenReturn(List.of(journal));
        when(movieClient.getMovies(200)).thenThrow(feignException);

        // Act
        List<Journal> result = journalService.getJournalsByUser(100);

        // Assert
        assertEquals(1, result.size());
        assertEquals(0, result.get(0).getMovies().size()); // Should be empty due to fallback
    }
    



}
