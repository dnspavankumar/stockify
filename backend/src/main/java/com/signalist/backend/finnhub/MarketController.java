package com.signalist.backend.finnhub;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

import com.signalist.backend.watchlist.WatchlistService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class MarketController {

    private static final String SESSION_HEADER = "X-Session-Token";

    private final FinnhubService finnhubService;
    private final WatchlistService watchlistService;

    public MarketController(FinnhubService finnhubService, WatchlistService watchlistService) {
        this.finnhubService = finnhubService;
        this.watchlistService = watchlistService;
    }

    @GetMapping("/stocks/search")
    public List<FinnhubDtos.StockResult> searchStocks(
            @RequestHeader(name = SESSION_HEADER, required = false) String sessionToken,
            @RequestParam(required = false) String query
    ) {
        Set<String> symbols = watchlistService.getSymbolsForSession(sessionToken);
        return finnhubService.searchStocks(query, symbols);
    }

    @GetMapping("/market/news")
    public List<FinnhubDtos.MarketNewsArticle> marketNews(@RequestParam(required = false) String symbols) {
        List<String> requestedSymbols = symbols == null || symbols.isBlank()
                ? List.of()
                : Arrays.stream(symbols.split(",")).toList();
        return finnhubService.getNews(requestedSymbols);
    }
}
