package com.demo.chatservice.external;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import com.demo.chatservice.dto.MovieDTO;

@FeignClient(name = "MOVIE-SERVICE")
public interface MovieClient {

    @GetMapping("/movie")
    List<MovieDTO> getAllMovies();
}