package com.signalist.backend.watchlist;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/watchlist")
public class WatchlistController {

    private static final String SESSION_HEADER = "X-Session-Token";

    private final WatchlistService watchlistService;

    public WatchlistController(WatchlistService watchlistService) {
        this.watchlistService = watchlistService;
    }

    @GetMapping
    public List<WatchlistDtos.WatchlistItemResponse> getWatchlist(
            @RequestHeader(name = SESSION_HEADER, required = false) String sessionToken
    ) {
        return watchlistService.getWatchlist(sessionToken);
    }

    @GetMapping("/status")
    public WatchlistDtos.WatchlistStatusResponse status(
            @RequestHeader(name = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam String symbol
    ) {
        return new WatchlistDtos.WatchlistStatusResponse(symbol.toUpperCase(), watchlistService.isInWatchlist(sessionToken, symbol));
    }

    @PostMapping
    public WatchlistDtos.WatchlistItemResponse add(
            @RequestHeader(name = SESSION_HEADER, required = false) String sessionToken,
            @Valid @RequestBody WatchlistDtos.WatchlistRequest request
    ) {
        return watchlistService.add(sessionToken, request);
    }

    @DeleteMapping("/{symbol}")
    public ResponseEntity<Void> remove(
            @RequestHeader(name = SESSION_HEADER, required = false) String sessionToken,
            @PathVariable String symbol
    ) {
        watchlistService.remove(sessionToken, symbol);
        return ResponseEntity.noContent().build();
    }
}
