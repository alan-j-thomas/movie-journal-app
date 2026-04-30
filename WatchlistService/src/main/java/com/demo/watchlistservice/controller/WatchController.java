package com.demo.watchlistservice.controller;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.demo.watchlistservice.dto.WatchlistRequest;
import com.demo.watchlistservice.entity.WatchList;
import com.demo.watchlistservice.service.WatchServiceImpl;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;

//@CrossOrigin("http://localhost:5173")
@RestController
@RequestMapping("/watchlist")
@Slf4j
public class WatchController {

    
    private WatchServiceImpl watchService;

    public WatchController(WatchServiceImpl watchService) {
		this.watchService = watchService;
	}

	@PostMapping
    public ResponseEntity<Object> addWatchList(@Valid @RequestBody WatchList watchList, BindingResult result) {
    	

		if (result.hasErrors()) {
		    FieldError fieldError = result.getFieldError();
		    String errorMsg = (fieldError != null) ? fieldError.getDefaultMessage() : "Validation error";
		    return ResponseEntity.badRequest().body(errorMsg);
		}

		return ResponseEntity.ok(watchService.addWatchList(watchList));
    	
    }

    @PostMapping("/add")
    public ResponseEntity<Object> addToWatchlist(@Valid @RequestBody WatchlistRequest request, BindingResult result) {
    	
    	if (result.hasErrors()) {
		    FieldError fieldError = result.getFieldError();
		    String errorMsg = (fieldError != null) ? fieldError.getDefaultMessage() : "Validation error";
		    return ResponseEntity.badRequest().body(errorMsg);
		}

        WatchList saved = watchService.addToWatchList(request);
        return ResponseEntity.ok(saved);
    }


    @GetMapping
    public ResponseEntity<List<WatchList>> getAllWatchLists() {
        return ResponseEntity.ok(watchService.getAllWatchLists());
    }
    

    @GetMapping("/{watchId}")
    @RateLimiter(name = "watchlistRateLimiter", fallbackMethod = "watchlistFallBack")
    public ResponseEntity<WatchList> getSingleWatchList(@PathVariable int watchId) {
        return ResponseEntity.ok(watchService.getSingleWatchList(watchId));
    }
    
    
    @SuppressWarnings("unused")
    public ResponseEntity<WatchList> watchlistFallBack(Throwable throwable) {
        WatchList watchList = WatchList.builder()
        		.movieId(0)
        		.note("Dummy")
        		.status(null)
        		.movies(Collections.emptyList())
        		.watchId(0)
        		.build();
        
        log.warn("Fallback triggered due to: {}", throwable.getMessage());
        
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(watchList);
        		
    }

    @GetMapping("/users/{userId}")
    public ResponseEntity<List<WatchList>> getWatchListsByUser(@PathVariable int userId) {
        return ResponseEntity.ok(watchService.getWatchListsByUser(userId));
    }

    @DeleteMapping("/{watchId}")
    public ResponseEntity<String> deleteFromWatchList(@PathVariable int watchId){
        return ResponseEntity.ok(watchService.deleteFromWatchList(watchId));
    }

    @PutMapping("/{watchId}")
    public ResponseEntity<WatchList> updateWatchList(@PathVariable int watchId, @RequestBody WatchList watchList){
        return ResponseEntity.ok(watchService.updateWatchList(watchId, watchList));
    }
}
