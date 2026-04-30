package com.demo.journalservice.external;

import com.demo.journalservice.entity.Movie;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "MOVIE-SERVICE")
public interface MovieClient {

    @GetMapping("/movie/{movieId}")
    Movie getMovies(@PathVariable int movieId);
}
