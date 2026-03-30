package com.signalist.backend.finnhub;

import java.lang.reflect.Array;
import java.net.URI;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import com.signalist.backend.config.AppProperties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class FinnhubService {

    private static final Logger logger = LoggerFactory.getLogger(FinnhubService.class);
    private static final List<String> POPULAR_SYMBOLS = List.of(
            "AAPL", "MSFT", "GOOGL", "AMZN", "TSLA",
            "META", "NVDA", "NFLX", "ORCL", "CRM"
    );

    private final RestClient restClient;
    private final AppProperties appProperties;
    private final Clock clock;

    public FinnhubService(RestClient restClient, AppProperties appProperties, Clock clock) {
        this.restClient = restClient;
        this.appProperties = appProperties;
        this.clock = clock;
    }

    public List<FinnhubDtos.StockResult> searchStocks(String query, Set<String> watchlistSymbols) {
        if (appProperties.getFinnhub().getApiKey().isBlank()) {
            logger.warn("Finnhub API key is not configured. Returning empty stock results.");
            return List.of();
        }

        String trimmedQuery = query == null ? "" : query.trim();
        Set<String> safeWatchlist = watchlistSymbols == null ? Set.of() : new HashSet<>(watchlistSymbols);

        if (trimmedQuery.isEmpty()) {
            List<FinnhubDtos.StockResult> results = new ArrayList<>();
            for (String symbol : POPULAR_SYMBOLS) {
                fetchProfile(symbol).ifPresent(profile -> results.add(new FinnhubDtos.StockResult(
                        symbol,
                        profile.name() == null || profile.name().isBlank() ? symbol : profile.name(),
                        profile.exchange() == null || profile.exchange().isBlank() ? "US" : profile.exchange(),
                        "Common Stock",
                        safeWatchlist.contains(symbol)
                )));
            }
            return results;
        }

        URI uri = uriBuilder("/search")
                .queryParam("q", trimmedQuery)
                .build(true)
                .toUri();

        FinnhubDtos.SearchResponse response = restClient.get()
                .uri(uri)
                .retrieve()
                .body(FinnhubDtos.SearchResponse.class);

        if (response == null || response.result() == null) {
            return List.of();
        }

        return response.result().stream()
                .filter(Objects::nonNull)
                .map(item -> {
                    String symbol = defaultValue(item.symbol(), "").toUpperCase();
                    return new FinnhubDtos.StockResult(
                            symbol,
                            defaultValue(item.description(), symbol),
                            "US",
                            defaultValue(item.type(), "Stock"),
                            safeWatchlist.contains(symbol)
                    );
                })
                .filter(item -> !item.symbol().isBlank())
                .limit(15)
                .toList();
    }

    public List<FinnhubDtos.MarketNewsArticle> getNews(List<String> symbols) {
        if (appProperties.getFinnhub().getApiKey().isBlank()) {
            logger.warn("Finnhub API key is not configured. Returning empty market news.");
            return List.of();
        }

        List<String> cleanSymbols = symbols == null ? List.of() : symbols.stream()
                .filter(Objects::nonNull)
                .map(symbol -> symbol.trim().toUpperCase())
                .filter(symbol -> !symbol.isBlank())
                .distinct()
                .toList();

        if (!cleanSymbols.isEmpty()) {
            List<List<FinnhubDtos.RawNewsArticle>> companyNews = new ArrayList<>();
            LocalDate toDate = LocalDate.now(clock);
            LocalDate fromDate = toDate.minusDays(5);

            for (String symbol : cleanSymbols) {
                URI uri = uriBuilder("/company-news")
                        .queryParam("symbol", symbol)
                        .queryParam("from", fromDate)
                        .queryParam("to", toDate)
                        .build(true)
                        .toUri();
                FinnhubDtos.RawNewsArticle[] articles = safeGetArray(uri, FinnhubDtos.RawNewsArticle[].class);
                companyNews.add(filterArticles(articles));
            }

            List<FinnhubDtos.MarketNewsArticle> collected = new ArrayList<>();
            for (int round = 0; round < 6; round++) {
                for (int index = 0; index < cleanSymbols.size(); index++) {
                    List<FinnhubDtos.RawNewsArticle> articles = companyNews.get(index);
                    if (articles.isEmpty()) {
                        continue;
                    }
                    FinnhubDtos.RawNewsArticle article = articles.removeFirst();
                    collected.add(formatArticle(article, true, cleanSymbols.get(index), round));
                    if (collected.size() >= 6) {
                        return collected.stream()
                                .sorted((left, right) -> Long.compare(right.datetime(), left.datetime()))
                                .toList();
                    }
                }
            }

            if (!collected.isEmpty()) {
                return collected.stream()
                        .sorted((left, right) -> Long.compare(right.datetime(), left.datetime()))
                        .toList();
            }
        }

        URI generalUri = uriBuilder("/news")
                .queryParam("category", "general")
                .build(true)
                .toUri();
        FinnhubDtos.RawNewsArticle[] articles = safeGetArray(generalUri, FinnhubDtos.RawNewsArticle[].class);
        List<FinnhubDtos.RawNewsArticle> unique = filterArticles(articles);

        List<FinnhubDtos.MarketNewsArticle> formatted = new ArrayList<>();
        for (int index = 0; index < unique.size() && formatted.size() < 6; index++) {
            formatted.add(formatArticle(unique.get(index), false, null, index));
        }
        return formatted;
    }

    private Optional<FinnhubDtos.ProfileResponse> fetchProfile(String symbol) {
        try {
            URI uri = uriBuilder("/stock/profile2")
                    .queryParam("symbol", symbol)
                    .build(true)
                    .toUri();
            FinnhubDtos.ProfileResponse profile = restClient.get().uri(uri).retrieve().body(FinnhubDtos.ProfileResponse.class);
            return Optional.ofNullable(profile);
        } catch (Exception exception) {
            logger.warn("Failed to fetch profile for {}", symbol, exception);
            return Optional.empty();
        }
    }

    private <T> T[] safeGetArray(URI uri, Class<T[]> type) {
        try {
            T[] body = restClient.get().uri(uri).retrieve().body(type);
            return body == null ? emptyArray(type) : body;
        } catch (Exception exception) {
            logger.warn("Finnhub request failed for {}", uri, exception);
            return emptyArray(type);
        }
    }

    private List<FinnhubDtos.RawNewsArticle> filterArticles(FinnhubDtos.RawNewsArticle[] articles) {
        List<FinnhubDtos.RawNewsArticle> filtered = new ArrayList<>();
        if (articles == null) {
            return filtered;
        }

        Set<String> seen = new HashSet<>();
        for (FinnhubDtos.RawNewsArticle article : articles) {
            if (!isValidArticle(article)) {
                continue;
            }
            String key = article.id() + "|" + article.url() + "|" + article.headline();
            if (!seen.add(key)) {
                continue;
            }
            filtered.add(article);
        }
        return filtered;
    }

    private boolean isValidArticle(FinnhubDtos.RawNewsArticle article) {
        return article != null
                && article.headline() != null
                && !article.headline().isBlank()
                && article.summary() != null
                && !article.summary().isBlank()
                && article.url() != null
                && !article.url().isBlank()
                && article.datetime() != null;
    }

    private FinnhubDtos.MarketNewsArticle formatArticle(
            FinnhubDtos.RawNewsArticle article,
            boolean companyNews,
            String symbol,
            int index
    ) {
        long articleId = companyNews ? Math.abs((article.datetime() + "-" + symbol + "-" + index).hashCode()) : defaultLong(article.id(), index + 1L);
        String summary = article.summary().trim();
        int maxLength = companyNews ? 200 : 150;
        if (summary.length() > maxLength) {
            summary = summary.substring(0, maxLength) + "...";
        } else if (!summary.endsWith("...")) {
            summary = summary + "...";
        }

        return new FinnhubDtos.MarketNewsArticle(
                articleId,
                article.headline().trim(),
                summary,
                defaultValue(article.source(), companyNews ? "Company News" : "Market News"),
                article.url(),
                article.datetime(),
                companyNews ? "company" : defaultValue(article.category(), "general"),
                companyNews ? defaultValue(symbol, "") : defaultValue(article.related(), ""),
                defaultValue(article.image(), "")
        );
    }

    private UriComponentsBuilder uriBuilder(String path) {
        return UriComponentsBuilder.fromHttpUrl(appProperties.getFinnhub().getBaseUrl() + path)
                .queryParam("token", appProperties.getFinnhub().getApiKey());
    }

    private long defaultLong(Long value, long fallback) {
        return value == null ? fallback : value;
    }

    private String defaultValue(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }

    @SuppressWarnings("unchecked")
    private <T> T[] emptyArray(Class<T[]> type) {
        return (T[]) Array.newInstance(type.getComponentType(), 0);
    }
}
