package com.signalist.backend.watchlist;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface WatchlistRepository extends MongoRepository<WatchlistItemDocument, String> {

    List<WatchlistItemDocument> findByUserIdOrderByAddedAtDesc(String userId);

    List<WatchlistItemDocument> findByUserId(String userId);

    boolean existsByUserIdAndSymbol(String userId, String symbol);

    void deleteByUserIdAndSymbol(String userId, String symbol);
}
