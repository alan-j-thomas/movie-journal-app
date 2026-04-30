package com.demo.movieservice.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import com.demo.movieservice.entity.Movie;
import com.demo.movieservice.service.MovieService;
import com.fasterxml.jackson.databind.ObjectMapper;

@WebMvcTest(MovieController.class)
class MovieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MovieService movieService;

    @Autowired
    private ObjectMapper objectMapper;

    private Movie sampleMovie;

    @BeforeEach
    void setUp() {
        sampleMovie = new Movie();
        sampleMovie.setMovieId(1);
        sampleMovie.setTitle("Inception");
        sampleMovie.setGenre("Sci-Fi");
        sampleMovie.setDirector("Christopher Nolan");
    }

    @Test
    void testAddMovieSuccess() throws Exception {
        when(movieService.addMovie(any(Movie.class))).thenReturn(sampleMovie);

        mockMvc.perform(post("/movie")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMovie)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    void testGetAllMovies() throws Exception {
        when(movieService.getAllMovies()).thenReturn(List.of(sampleMovie));

        mockMvc.perform(get("/movie"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Inception"));
    }

    @Test
    void testGetMovieByTitleFound() throws Exception {
        when(movieService.findMovieByTitle("Inception")).thenReturn(sampleMovie);

        mockMvc.perform(get("/movie/title/Inception"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    void testGetMovieByTitleNotFound() throws Exception {
        when(movieService.findMovieByTitle("Unknown")).thenThrow(new RuntimeException());

        mockMvc.perform(get("/movie/title/Unknown"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetMovieByIdFound() throws Exception {
        when(movieService.getMovie(1)).thenReturn(sampleMovie);

        mockMvc.perform(get("/movie/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    void testGetMovieByIdNotFound() throws Exception {
        when(movieService.getMovie(99)).thenThrow(new RuntimeException());

        mockMvc.perform(get("/movie/99"))
                .andExpect(status().isNotFound());
    }

    @Test
    void testUpdateMovieSuccess() throws Exception {
        when(movieService.updateMovie(any(Movie.class), eq(1))).thenReturn(sampleMovie);

        mockMvc.perform(put("/movie/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMovie)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    void testUpdateMovieNotFound() throws Exception {
        when(movieService.updateMovie(any(Movie.class), eq(99))).thenThrow(new RuntimeException());

        mockMvc.perform(put("/movie/99")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleMovie)))
                .andExpect(status().isNotFound());
    }

    @Test
    void testAddMovieWithImageSuccess() throws Exception {
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "poster.jpg", "image/jpeg", "dummy".getBytes());
        MockMultipartFile moviePart = new MockMultipartFile("movie", "", "application/json", objectMapper.writeValueAsBytes(sampleMovie));

        when(movieService.addMovieWithImage(any(Movie.class), any())).thenReturn(sampleMovie);

        mockMvc.perform(multipart("/movie/add")
                .file(moviePart)
                .file(imageFile))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Inception"));
    }

    @Test
    void testGetImageByMovieIdSuccess() throws Exception {
        sampleMovie.setImageType("image/jpeg");
        sampleMovie.setImageData("dummy".getBytes());

        when(movieService.getMovie(1)).thenReturn(sampleMovie);

        mockMvc.perform(get("/movie/1/image"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("image/jpeg"));
    }

    @Test
    void testGetImageByMovieIdNotFound() throws Exception {
        when(movieService.getMovie(99)).thenReturn(null);

        mockMvc.perform(get("/movie/99/image"))
                .andExpect(status().isNotFound());
    }
    
    @Test
    void testAddMovieValidationError() throws Exception {
        Movie invalidMovie = new Movie();

        mockMvc.perform(post("/movie")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidMovie)))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("should not be blank")));
    }
    
    @Test
    void testAddMovieWithImage_ValidationError() throws Exception {
        Movie invalidMovie = new Movie(); // Missing required fields
        MockMultipartFile moviePart = new MockMultipartFile("movie", "", "application/json", objectMapper.writeValueAsBytes(invalidMovie));
        MockMultipartFile imageFile = new MockMultipartFile("imageFile", "poster.jpg", "image/jpeg", "dummy".getBytes());

        mockMvc.perform(multipart("/movie/add")
                .file(moviePart)
                .file(imageFile))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("")));
    }
    
   
}
