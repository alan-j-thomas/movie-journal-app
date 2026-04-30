package com.demo.watchlistservice.service;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import com.demo.watchlistservice.dto.WatchlistRequest;
import com.demo.watchlistservice.entity.Movie;
import com.demo.watchlistservice.entity.WatchList;
import com.demo.watchlistservice.exceptions.MovieNotFoundException;
import com.demo.watchlistservice.exceptions.WatchListNotFoundException;
import com.demo.watchlistservice.external.MovieClient;
import com.demo.watchlistservice.repository.WatchRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WatchServiceImpl implements WatchService{

    
    private final WatchRepository watchRepository;

    
    private final MovieClient movieClient;

    
    private final RestTemplate restTemplate;
    
    
    
    private Logger logger = LoggerFactory.getLogger(WatchServiceImpl.class);


    @Override
    public WatchList addWatchList(WatchList watchList) {
        logger.info("Adding new watchlist: {}", watchList);
        return watchRepository.save(watchList);
    }


    @Override
    public List<WatchList> getAllWatchLists() {
        logger.info("Fetching all watchlists");
        List<WatchList> allWatchLists = watchRepository.findAll();

        return allWatchLists.stream().map(watchList -> {
            Movie movie = movieClient.getMovies(watchList.getMovieId());
            watchList.setMovies(List.of(movie));
            return watchList;
        }).toList();
    }

    @Override
    public WatchList getSingleWatchList(int watchId) {
        logger.info("Fetching single watchlist with id: {}", watchId);
        
        WatchList watchList = watchRepository.findById(watchId).orElseThrow(() -> {
            logger.warn("Watchlist not found with id: {}", watchId);
            return new WatchListNotFoundException();
        });
        Movie movie = movieClient.getMovies(watchList.getMovieId());
        watchList.setMovies(List.of(movie));
        
        return watchList;
    }

    @Override
    public List<WatchList> getWatchListsByUser(int userId) {
    	
        logger.info("Fetching watchlists for userId: {}", userId);
        List<WatchList> watchLists = watchRepository.getWatchListsByUserId(userId);

        return watchLists.stream().map(watchList -> {
        	
            Movie movie = movieClient.getMovies(watchList.getMovieId());
            watchList.setMovies(List.of(movie));
            return watchList;
            
        }).toList();
    }


    @Override
    public WatchList addToWatchList(WatchlistRequest request) {
        logger.info("Adding to watchlist for userId: {}, movieTitle: {}", request.getUserId(), request.getMovieTitle());
        Movie movie = restTemplate.getForObject(
                "http://MOVIE-SERVICE/movie/title/" + request.getMovieTitle(),
                Movie.class
        );

        if (movie == null || movie.getMovieId() == 0) {
            logger.warn("Movie not found for title: {}", request.getMovieTitle());
            throw new MovieNotFoundException();
        }

        WatchList watchList = new WatchList();
        watchList.setUserId(request.getUserId());
        watchList.setMovieId(movie.getMovieId());
        watchList.setStatus(request.getStatus());
        watchList.setNote(request.getNote());

        logger.info("Saving watchlist: {}", watchList);
        return watchRepository.save(watchList);
    }

    @Override
    public String deleteFromWatchList(int watchlistId) {
        WatchList watchList = watchRepository.findById(watchlistId).orElseThrow(() -> {
            logger.warn("Watchlist not found for delete with id: {}", watchlistId);
            return new WatchListNotFoundException();
        });
        watchRepository.delete(watchList);
        
        logger.info("Watchlist deleted successfully with id: {}", watchlistId);
        return "Deleted!";
    }

    @Override
    public WatchList updateWatchList(int watchId, WatchList watchList) {
        WatchList watchList1 = watchRepository.findById(watchId).orElseThrow(() -> {
        	
            logger.warn("Watchlist not found for update with id: {}", watchId);
            return new WatchListNotFoundException();
            
        });
        watchList1.setStatus(watchList.getStatus());
        watchList1.setNote(watchList.getNote());
        
        WatchList updated = watchRepository.save(watchList1);
        
        logger.info("Updated watchlist: {}", updated);
        return updated;
    }
}
