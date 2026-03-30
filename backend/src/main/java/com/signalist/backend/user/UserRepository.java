package com.signalist.backend.user;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

public interface UserRepository extends MongoRepository<UserDocument, String> {

    Optional<UserDocument> findByEmailIgnoreCase(String email);

    List<UserDocument> findAllByEmailNotNull();
}
