package com.kratee.copilot.auth;

import com.kratee.copilot.user.User;
import com.kratee.copilot.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

// AuthService.java
@Service
public class AuthService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthService(UserRepository repository, PasswordEncoder encoder, JwtService jwtService) {
        this.repository = repository;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    public TokenResponse login(LoginRequest request) {
        User user = repository.findByEmail(request.email())
                .orElseThrow(InvalidCredentialsException::new);
        if (!encoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }
        return new TokenResponse(jwtService.generate(user));
    }
}