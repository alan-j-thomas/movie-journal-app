package com.demo.watchlistservice.service;

import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.demo.watchlistservice.entity.Movie;
import com.demo.watchlistservice.entity.Status;
import com.demo.watchlistservice.entity.WatchList;
import com.demo.watchlistservice.external.MovieClient;
import com.demo.watchlistservice.repository.WatchRepository;



@ExtendWith(MockitoExtension.class)
class WatchServiceTest {

	@Mock
	WatchRepository watchRepository;
	
	@Mock
	MovieClient movieClient;
	
	@InjectMocks
	WatchServiceImpl watchService;
	
	@Test
	void TestAddToWatchlist() {
		
		WatchList watchList = WatchList.builder()
				.watchId(1)
				.status(Status.PLANNED)
				.note("Planning to watch this")
				.userId(1)
				.movieId(2)
				.build();
		
		Mockito.when(watchRepository.save(watchList)).thenReturn(watchList);
		
		WatchList addedList = watchService.addWatchList(watchList);
		
		Assertions.assertEquals(watchList.getMovieId(), addedList.getMovieId());
		Assertions.assertEquals(Status.PLANNED, addedList.getStatus());
	}
	
	@Test
	void testGetWatchlistByUserId() {
	    int userId = 1;
	    WatchList watchList1 = WatchList.builder()
	            .watchId(1)
	            .status(Status.PLANNED)
	            .note("First movie")
	            .userId(userId)
	            .movieId(2)
	            .build();
	    
	    WatchList watchList2 = WatchList.builder()
	            .watchId(2)
	            .status(Status.COMPLETED)
	            .note("Second movie")
	            .userId(userId)
	            .movieId(3)
	            .build();

	    Mockito.when(watchRepository.getWatchListsByUserId(userId)).thenReturn(List.of(watchList1, watchList2));

	    
	    Mockito.when(movieClient.getMovies(2)).thenReturn(new Movie());
	    Mockito.when(movieClient.getMovies(3)).thenReturn(new Movie());
	    
	    List<WatchList> result = watchService.getWatchListsByUser(userId);

	    Assertions.assertEquals(2, result.size());
	    Assertions.assertEquals(Status.PLANNED, result.get(0).getStatus());
	    Assertions.assertEquals(Status.COMPLETED, result.get(1).getStatus());
	}


}
