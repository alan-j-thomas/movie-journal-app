package com.demo.watchlistservice.external;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.watchlistservice.entity.Movie;

@FeignClient(name = "MOVIE-SERVICE")
public interface MovieClient {

    @GetMapping("/movie/{movieId}")
    Movie getMovies(@PathVariable int movieId);
}
