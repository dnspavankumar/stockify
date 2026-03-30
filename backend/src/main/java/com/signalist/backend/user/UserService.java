package com.signalist.backend.user;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

import com.signalist.backend.auth.AuthDtos;
import com.signalist.backend.common.ApiException;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final Clock clock;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, Clock clock) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.clock = clock;
    }

    public UserDocument register(AuthDtos.SignUpRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        userRepository.findByEmailIgnoreCase(normalizedEmail).ifPresent(existing -> {
            throw new ApiException(HttpStatus.CONFLICT, "An account with this email already exists");
        });

        UserDocument user = new UserDocument();
        user.setEmail(normalizedEmail);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setName(request.fullName().trim());
        user.setCountry(request.country().trim());
        user.setInvestmentGoals(request.investmentGoals().trim());
        user.setRiskTolerance(request.riskTolerance().trim());
        user.setPreferredIndustry(request.preferredIndustry().trim());
        user.setCreatedAt(Instant.now(clock));
        return userRepository.save(user);
    }

    public UserDocument authenticate(String email, String password) {
        UserDocument user = findByEmail(email);
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return user;
    }

    public UserDocument findById(String id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "User not found"));
    }

    public UserDocument findByEmail(String email) {
        return userRepository.findByEmailIgnoreCase(normalizeEmail(email))
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
    }

    public List<UserDocument> getAllUsersWithEmail() {
        return userRepository.findAllByEmailNotNull();
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }
}
