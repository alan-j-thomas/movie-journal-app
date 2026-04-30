package com.demo.userservice.external;

import com.demo.userservice.entity.Watchlist;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "WATCHLIST-SERVICE")
public interface WatchListClient {

    @GetMapping("/watchlist/users/{userId}")
    List<Watchlist> getWatchListsByUser(@PathVariable int userId);
}
