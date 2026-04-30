package com.demo.movieservice.service;

import com.demo.movieservice.entity.Movie;
import com.demo.movieservice.exceptions.MovieNotFoundException;
import com.demo.movieservice.repository.MovieRepository;

import lombok.RequiredArgsConstructor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieServiceImpl implements MovieService{

    
    private final MovieRepository movieRepository;
    
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    @Override
    public Movie addMovie(Movie movie) {
    	
    	logger.info("Adding new movie: {}", movie.getTitle());
    	Movie addedMovie = movieRepository.save(movie);
    	
    	logger.debug("New movie added: {}", addedMovie.getMovieId());
        return addedMovie;
    }



    @Override
    public List<Movie> getAllMovies() {
        logger.info("Fetching all movies");
        List<Movie> movies = movieRepository.findAll();
        
        logger.debug("Total movies found: {}", movies.size());
        return movies;
    }

    @Override
    public Movie getMovie(int movieId) {
        logger.info("Fetching movie with ID: {}", movieId);
        return movieRepository.findById(movieId)
                .orElseThrow(() -> {
                    logger.warn("Movie not found with ID: {}", movieId);
                    return new MovieNotFoundException("Movie not found with ID: " + movieId);
                });
    }

    @Override
    public Movie findMovieByTitle(String title) {
        logger.info("Searching for movie by title: {}", title);
        return movieRepository.findMovieByTitleIgnoreCase(title)
                .orElseThrow(() -> {
                    logger.warn("Movie not found with title: {}", title);
                    return new MovieNotFoundException("Movie not found with title: " + title);
                });
    }

    @Override
    public Movie addMovieWithImage(Movie movie, MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            throw new IllegalArgumentException("Image file cannot be null or empty");
        }
        
        logger.info("Adding movie with image: {}", movie.getTitle());
        movie.setImageName(imageFile.getOriginalFilename());
        movie.setImageType(imageFile.getContentType());
        movie.setImageData(imageFile.getBytes());
        
        return movieRepository.save(movie);
    }



	@Override
	public Movie updateMovie(Movie movie, int movieId) {
		logger.info("Updating movie with ID: {}", movieId);
		
		Movie existingMovie = movieRepository.findById(movieId)
			.orElseThrow(() -> new MovieNotFoundException("Movie not found with ID: " + movieId));
		
		existingMovie.setTitle(movie.getTitle());
		existingMovie.setCast(movie.getCast());
		existingMovie.setGenre(movie.getGenre());
		existingMovie.setDirector(movie.getDirector());
		existingMovie.setLanguage(movie.getLanguage());
		existingMovie.setRating(movie.getRating());
		existingMovie.setSummary(movie.getSummary());
		
		return movieRepository.save(existingMovie);
	}
}
