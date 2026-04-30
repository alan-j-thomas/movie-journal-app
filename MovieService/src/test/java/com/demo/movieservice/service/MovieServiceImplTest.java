package com.demo.movieservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import com.demo.movieservice.entity.Movie;
import com.demo.movieservice.exceptions.MovieNotFoundException;
import com.demo.movieservice.repository.MovieRepository;

class MovieServiceImplTest {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieServiceImpl movieService;

    private Movie sampleMovie;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleMovie = new Movie();
        sampleMovie.setMovieId(1);
        sampleMovie.setTitle("Inception");
        sampleMovie.setGenre("Sci-Fi");
        sampleMovie.setDirector("Christopher Nolan");
    }

    @Test
    void testAddMovie() {
        when(movieRepository.save(sampleMovie)).thenReturn(sampleMovie);
        Movie result = movieService.addMovie(sampleMovie);
        assertEquals("Inception", result.getTitle());
        verify(movieRepository, times(1)).save(sampleMovie);
    }

    @Test
    void testGetAllMovies() {
        List<Movie> movies = Arrays.asList(sampleMovie);
        when(movieRepository.findAll()).thenReturn(movies);
        List<Movie> result = movieService.getAllMovies();
        assertEquals(1, result.size());
        verify(movieRepository).findAll();
    }

    @Test
    void testGetMovieFound() {
        when(movieRepository.findById(1)).thenReturn(Optional.of(sampleMovie));
        Movie result = movieService.getMovie(1);
        assertEquals("Inception", result.getTitle());
    }

    @Test
    void testGetMovieNotFound() {
        when(movieRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(MovieNotFoundException.class, () -> movieService.getMovie(1));
    }

    @Test
    void testFindMovieByTitleFound() {
        when(movieRepository.findMovieByTitleIgnoreCase("Inception")).thenReturn(Optional.of(sampleMovie));
        Movie result = movieService.findMovieByTitle("Inception");
        assertEquals("Inception", result.getTitle());
    }

    @Test
    void testFindMovieByTitleNotFound() {
        when(movieRepository.findMovieByTitleIgnoreCase("Unknown")).thenReturn(Optional.empty());
        assertThrows(MovieNotFoundException.class, () -> movieService.findMovieByTitle("Unknown"));
    }

    @Test
    void testAddMovieWithImage() throws IOException {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(false);
        when(mockFile.getOriginalFilename()).thenReturn("poster.jpg");
        when(mockFile.getContentType()).thenReturn("image/jpeg");
        when(mockFile.getBytes()).thenReturn("image-data".getBytes());

        when(movieRepository.save(any(Movie.class))).thenReturn(sampleMovie);

        Movie result = movieService.addMovieWithImage(sampleMovie, mockFile);
        assertEquals("poster.jpg", result.getImageName());
        verify(movieRepository).save(any(Movie.class));
    }

    @Test
    void testAddMovieWithEmptyImageThrowsException() {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.isEmpty()).thenReturn(true);
        assertThrows(IllegalArgumentException.class, () -> movieService.addMovieWithImage(sampleMovie, mockFile));
    }

    @Test
    void testUpdateMovieSuccess() {
        Movie updated = new Movie();
        updated.setTitle("Interstellar");
        updated.setGenre("Sci-Fi");

        when(movieRepository.findById(1)).thenReturn(Optional.of(sampleMovie));
        when(movieRepository.save(any(Movie.class))).thenReturn(updated);

        Movie result = movieService.updateMovie(updated, 1);
        assertEquals("Interstellar", result.getTitle());
    }

    @Test
    void testUpdateMovieNotFound() {
        when(movieRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(MovieNotFoundException.class, () -> movieService.updateMovie(sampleMovie, 1));
    }
}