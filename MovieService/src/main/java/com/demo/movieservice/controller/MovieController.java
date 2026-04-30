package com.demo.movieservice.controller;

import com.demo.movieservice.entity.Movie;
import com.demo.movieservice.service.MovieService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/movie")
@RequiredArgsConstructor
public class MovieController {

    
    private final MovieService movieService;

    @PostMapping
    public ResponseEntity<Object> addMovie(@Valid @RequestBody Movie movie, BindingResult result){
    	
    	if (result.hasErrors()) {
		    FieldError fieldError = result.getFieldError();
		    String errorMsg = (fieldError != null) ? fieldError.getDefaultMessage() : "Validation error";
		    return ResponseEntity.badRequest().body(errorMsg);
		}

        return new ResponseEntity<>(movieService.addMovie(movie), HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<Movie>> getAllMovies(){
        return new ResponseEntity<>(movieService.getAllMovies(), HttpStatus.OK);
    }

    @GetMapping("/title/{title}")
    public ResponseEntity<Movie> getMovieByTitle(@PathVariable String title) {
        try {
            Movie movie = movieService.findMovieByTitle(title);
            return new ResponseEntity<>(movie, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping("/{movieId}")
    public ResponseEntity<Movie> getMovie(@PathVariable int movieId){
        try {
            Movie movie = movieService.getMovie(movieId);
            return new ResponseEntity<>(movie, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    @PutMapping("/{movieId}")
    public ResponseEntity<Movie> updateMovie(@RequestBody Movie movie, @PathVariable int movieId){
        try {
            Movie updatedMovie = movieService.updateMovie(movie, movieId);
            return new ResponseEntity<>(updatedMovie, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
    
    //Update- for adding movie with an image
    @PostMapping("/add")
    public ResponseEntity<Object> addMovieWithImage(@Valid @RequestPart Movie movie, @RequestPart MultipartFile imageFile){

    	try {
    		Movie movieData = movieService.addMovieWithImage(movie, imageFile);
        	return new ResponseEntity<>(movieData, HttpStatus.CREATED);
    	}
    	catch (IllegalArgumentException e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
		} catch (Exception e) {
			return new ResponseEntity<>(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
		}
    	
    }
    
    @GetMapping("/{movieId}/image")
    public ResponseEntity<byte[]> getImageByMovieId(@PathVariable int movieId){
        try {
            Movie movie = movieService.getMovie(movieId);
            
            if (movie == null || movie.getImageData() == null) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            
            byte[] imageFile = movie.getImageData();
            
            return ResponseEntity.ok().contentType(MediaType.valueOf(movie.getImageType()))
                    .body(imageFile);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
