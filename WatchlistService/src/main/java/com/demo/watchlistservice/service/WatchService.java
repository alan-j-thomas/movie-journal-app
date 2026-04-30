package com.demo.watchlistservice.service;

import java.util.List;

import com.demo.watchlistservice.dto.WatchlistRequest;
import com.demo.watchlistservice.entity.WatchList;


public interface WatchService {
    WatchList addWatchList(WatchList watchList);
    List<WatchList> getAllWatchLists();
    WatchList getSingleWatchList(int watchId);
    List<WatchList> getWatchListsByUser(int userId);
    WatchList addToWatchList(WatchlistRequest request);
    String deleteFromWatchList(int watchlistId);
    WatchList updateWatchList(int watchId, WatchList watchList);
}
