package com.signalist.backend.watchlist;

import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import com.signalist.backend.session.SessionService;
import com.signalist.backend.user.UserDocument;

import org.springframework.stereotype.Service;

@Service
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final SessionService sessionService;
    private final Clock clock;

    public WatchlistService(WatchlistRepository watchlistRepository, SessionService sessionService, Clock clock) {
        this.watchlistRepository = watchlistRepository;
        this.sessionService = sessionService;
        this.clock = clock;
    }

    public List<WatchlistDtos.WatchlistItemResponse> getWatchlist(String sessionToken) {
        UserDocument user = sessionService.requireUser(sessionToken);
        return watchlistRepository.findByUserIdOrderByAddedAtDesc(user.getId())
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public WatchlistDtos.WatchlistItemResponse add(String sessionToken, WatchlistDtos.WatchlistRequest request) {
        UserDocument user = sessionService.requireUser(sessionToken);
        String normalizedSymbol = request.symbol().trim().toUpperCase();

        if (watchlistRepository.existsByUserIdAndSymbol(user.getId(), normalizedSymbol)) {
            return watchlistRepository.findByUserIdOrderByAddedAtDesc(user.getId())
                    .stream()
                    .filter(item -> normalizedSymbol.equals(item.getSymbol()))
                    .findFirst()
                    .map(this::toResponse)
                    .orElseGet(() -> new WatchlistDtos.WatchlistItemResponse(
                            null,
                            user.getId(),
                            normalizedSymbol,
                            request.company().trim(),
                            Instant.now(clock)
                    ));
        }

        WatchlistItemDocument item = new WatchlistItemDocument();
        item.setUserId(user.getId());
        item.setSymbol(normalizedSymbol);
        item.setCompany(request.company().trim());
        item.setAddedAt(Instant.now(clock));
        return toResponse(watchlistRepository.save(item));
    }

    public void remove(String sessionToken, String symbol) {
        UserDocument user = sessionService.requireUser(sessionToken);
        watchlistRepository.deleteByUserIdAndSymbol(user.getId(), symbol.trim().toUpperCase());
    }

    public boolean isInWatchlist(String sessionToken, String symbol) {
        UserDocument user = sessionService.requireUser(sessionToken);
        return watchlistRepository.existsByUserIdAndSymbol(user.getId(), symbol.trim().toUpperCase());
    }

    public Set<String> getSymbolsForSession(String sessionToken) {
        return sessionService.resolveUser(sessionToken)
                .map(user -> getSymbolsForUser(user.getId()))
                .orElseGet(Set::of);
    }

    public Set<String> getSymbolsForUser(String userId) {
        return watchlistRepository.findByUserId(userId)
                .stream()
                .map(WatchlistItemDocument::getSymbol)
                .collect(Collectors.toSet());
    }

    private WatchlistDtos.WatchlistItemResponse toResponse(WatchlistItemDocument item) {
        return new WatchlistDtos.WatchlistItemResponse(
                item.getId(),
                item.getUserId(),
                item.getSymbol(),
                item.getCompany(),
                item.getAddedAt()
        );
    }
}
