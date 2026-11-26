package com.kratee.copilot.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository repository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    public UserResponse register(RegisterRequest request) {
        if (repository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setPasswordHash(encoder.encode(request.password()));
        User saved = repository.save(user);
        return new UserResponse(saved.getId(), saved.getEmail());
    }
}