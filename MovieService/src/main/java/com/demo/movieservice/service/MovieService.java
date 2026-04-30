package com.demo.movieservice.service;

import com.demo.movieservice.entity.Movie;

import java.io.IOException;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

public interface MovieService {

    Movie addMovie(Movie movie);
    List<Movie> getAllMovies();
    Movie getMovie(int movieId);
    Movie findMovieByTitle(String title);
    Movie addMovieWithImage(Movie movie, MultipartFile imageFile) throws IOException;
    Movie updateMovie(Movie movie, int movieId);
}
