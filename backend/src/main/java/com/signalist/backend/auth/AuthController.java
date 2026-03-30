package com.signalist.backend.auth;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private static final String SESSION_HEADER = "X-Session-Token";

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-up")
    public AuthDtos.AuthResponse signUp(@Valid @RequestBody AuthDtos.SignUpRequest request) {
        return authService.signUp(request);
    }

    @PostMapping("/sign-in")
    public AuthDtos.AuthResponse signIn(@Valid @RequestBody AuthDtos.SignInRequest request) {
        return authService.signIn(request);
    }

    @PostMapping("/sign-out")
    public ResponseEntity<Void> signOut(@RequestHeader(name = SESSION_HEADER, required = false) String token) {
        authService.signOut(token);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/session")
    public AuthDtos.SessionResponse session(@RequestHeader(name = SESSION_HEADER, required = false) String token) {
        return authService.getSession(token);
    }
}
