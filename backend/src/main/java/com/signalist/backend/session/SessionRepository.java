package com.signalist.backend.session;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface SessionRepository extends MongoRepository<SessionDocument, String> {

    Optional<SessionDocument> findByToken(String token);

    void deleteByToken(String token);

    void deleteByExpiresAtBefore(Instant timestamp);
}
