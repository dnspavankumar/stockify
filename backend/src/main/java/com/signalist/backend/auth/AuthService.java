package com.signalist.backend.auth;

import com.signalist.backend.mail.MailService;
import com.signalist.backend.session.SessionDocument;
import com.signalist.backend.session.SessionService;
import com.signalist.backend.user.UserDocument;
import com.signalist.backend.user.UserService;

import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserService userService;
    private final SessionService sessionService;
    private final MailService mailService;

    public AuthService(UserService userService, SessionService sessionService, MailService mailService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.mailService = mailService;
    }

    public AuthDtos.AuthResponse signUp(AuthDtos.SignUpRequest request) {
        UserDocument user = userService.register(request);
        SessionDocument session = sessionService.createSession(user);
        mailService.sendWelcomeEmail(user);
        return toAuthResponse(user, session);
    }

    public AuthDtos.AuthResponse signIn(AuthDtos.SignInRequest request) {
        UserDocument user = userService.authenticate(request.email(), request.password());
        SessionDocument session = sessionService.createSession(user);
        return toAuthResponse(user, session);
    }

    public void signOut(String token) {
        sessionService.invalidate(token);
    }

    public AuthDtos.SessionResponse getSession(String token) {
        UserDocument user = sessionService.requireUser(token);
        return new AuthDtos.SessionResponse(toUserResponse(user));
    }

    private AuthDtos.AuthResponse toAuthResponse(UserDocument user, SessionDocument session) {
        return new AuthDtos.AuthResponse(toUserResponse(user), session.getToken(), session.getExpiresAt());
    }

    private AuthDtos.UserResponse toUserResponse(UserDocument user) {
        return new AuthDtos.UserResponse(user.getId(), user.getName(), user.getEmail());
    }
}
