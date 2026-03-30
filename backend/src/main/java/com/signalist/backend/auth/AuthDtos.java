package com.signalist.backend.auth;

import java.time.Instant;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public final class AuthDtos {

    private AuthDtos() {
    }

    public record SignUpRequest(
            @NotBlank(message = "Full name is required") @Size(min = 2, message = "Full name is too short") String fullName,
            @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
            @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters") String password,
            @NotBlank(message = "Country is required") String country,
            @NotBlank(message = "Investment goals are required") String investmentGoals,
            @NotBlank(message = "Risk tolerance is required") String riskTolerance,
            @NotBlank(message = "Preferred industry is required") String preferredIndustry
    ) {
    }

    public record SignInRequest(
            @NotBlank(message = "Email is required") @Email(message = "Email must be valid") String email,
            @NotBlank(message = "Password is required") String password
    ) {
    }

    public record UserResponse(String id, String name, String email) {
    }

    public record AuthResponse(UserResponse user, String sessionToken, Instant expiresAt) {
    }

    public record SessionResponse(UserResponse user) {
    }
}
