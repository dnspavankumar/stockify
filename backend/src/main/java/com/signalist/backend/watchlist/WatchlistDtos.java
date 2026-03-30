package com.signalist.backend.watchlist;

import java.time.Instant;

import jakarta.validation.constraints.NotBlank;

public final class WatchlistDtos {

    private WatchlistDtos() {
    }

    public record WatchlistRequest(
            @NotBlank(message = "Symbol is required") String symbol,
            @NotBlank(message = "Company is required") String company
    ) {
    }

    public record WatchlistItemResponse(
            String id,
            String userId,
            String symbol,
            String company,
            Instant addedAt
    ) {
    }

    public record WatchlistStatusResponse(String symbol, boolean isInWatchlist) {
    }
}
