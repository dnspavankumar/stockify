package com.signalist.backend.session;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import com.signalist.backend.common.ApiException;
import com.signalist.backend.config.AppProperties;
import com.signalist.backend.user.UserDocument;
import com.signalist.backend.user.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final SessionRepository sessionRepository;
    private final UserService userService;
    private final AppProperties appProperties;
    private final SecureRandom secureRandom;
    private final Clock clock;

    public SessionService(
            SessionRepository sessionRepository,
            UserService userService,
            AppProperties appProperties,
            SecureRandom secureRandom,
            Clock clock
    ) {
        this.sessionRepository = sessionRepository;
        this.userService = userService;
        this.appProperties = appProperties;
        this.secureRandom = secureRandom;
        this.clock = clock;
    }

    public SessionDocument createSession(UserDocument user) {
        sessionRepository.deleteByExpiresAtBefore(Instant.now(clock));

        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        SessionDocument session = new SessionDocument();
        session.setToken(Base64.getUrlEncoder().withoutPadding().encodeToString(bytes));
        session.setUserId(user.getId());
        session.setCreatedAt(Instant.now(clock));
        session.setExpiresAt(Instant.now(clock).plus(appProperties.getSession().getTtl()));
        return sessionRepository.save(session);
    }

    public void invalidate(String token) {
        if (token != null && !token.isBlank()) {
            sessionRepository.deleteByToken(token);
        }
    }

    public Optional<UserDocument> resolveUser(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        Optional<SessionDocument> session = sessionRepository.findByToken(token);
        if (session.isEmpty()) {
            return Optional.empty();
        }

        if (session.get().getExpiresAt().isBefore(Instant.now(clock))) {
            sessionRepository.deleteByToken(token);
            return Optional.empty();
        }

        return Optional.of(userService.findById(session.get().getUserId()));
    }

    public UserDocument requireUser(String token) {
        return resolveUser(token)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Authentication required"));
    }
}
