package com.demo.chatservice.external;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.demo.chatservice.dto.WatchlistMovieDTO;

@FeignClient(name = "WATCHLIST-SERVICE")
public interface WatchlistClient {

    @GetMapping("/watchlist/users/{userId}")
    List<WatchlistMovieDTO> getWatchListsByUser(@PathVariable int userId);
}