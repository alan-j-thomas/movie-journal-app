package com.demo.watchlistservice.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import com.demo.watchlistservice.entity.Status;
import com.demo.watchlistservice.entity.WatchList;




@DataJpaTest
class WatchRepositoryTest {
	
	
	@Autowired
	private WatchRepository watchRepository;

	@Test
	void getWatchlistByUserTest() {
		
		WatchList w1 = WatchList.builder()
				.userId(1)
				.movieId(1)
				.status(Status.PLANNED)
				.note("Planning to watch")
				.build();
		
		WatchList w2 = WatchList.builder()
				.userId(1)
				.movieId(2)
				.status(Status.WATCHING)
				.note("Currently watching this movie")
				.build();
		
		watchRepository.save(w1);
		watchRepository.save(w2);
		
		List<WatchList> lst = watchRepository.getWatchListsByUserId(1);
		
		System.out.println("Retrieved List: " + lst.toString());
		assertThat(lst).isNotEmpty().hasSize(2);
		
		assertThat(lst.get(0).getUserId()).isEqualTo(1);
		assertThat(lst.get(1).getUserId()).isEqualTo(1);
				
				
	}

}
