package com.signalist.backend.finnhub;

import java.util.List;

public final class FinnhubDtos {

    private FinnhubDtos() {
    }

    public record StockResult(
            String symbol,
            String name,
            String exchange,
            String type,
            boolean isInWatchlist
    ) {
    }

    public record MarketNewsArticle(
            long id,
            String headline,
            String summary,
            String source,
            String url,
            long datetime,
            String category,
            String related,
            String image
    ) {
    }

    record SearchResponse(Integer count, List<SearchResult> result) {
    }

    record SearchResult(String symbol, String description, String displaySymbol, String type) {
    }

    record ProfileResponse(String ticker, String name, String exchange) {
    }

    record RawNewsArticle(Long id, String headline, String summary, String source, String url, Long datetime, String image, String category, String related) {
    }
}
