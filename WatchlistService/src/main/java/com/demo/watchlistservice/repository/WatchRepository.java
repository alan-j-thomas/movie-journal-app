package com.demo.watchlistservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.demo.watchlistservice.entity.WatchList;

@Repository
public interface WatchRepository extends JpaRepository<WatchList, Integer> {

    List<WatchList> getWatchListsByUserId(int userId);

}
