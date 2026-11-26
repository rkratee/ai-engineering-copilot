package com.kratee.copilot.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.kratee.copilot.user.User;
import com.kratee.copilot.user.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock UserRepository repository;
    @Mock PasswordEncoder encoder;
    @Mock JwtService jwtService;
    @InjectMocks AuthService service;

    private User existingUser() {
        User user = new User();
        user.setId(1L);
        user.setEmail("a@b.com");
        user.setPasswordHash("HASH");
        return user;
    }

    @Test
    void login_success_returnsToken() {
        User user = existingUser();
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.of(user));
        when(encoder.matches("password123", "HASH")).thenReturn(true);
        when(jwtService.generate(user)).thenReturn("TOKEN");

        TokenResponse response = service.login(new LoginRequest("a@b.com", "password123"));

        assertEquals("TOKEN", response.accessToken());
    }

    @Test
    void login_unknownEmail_throws() {
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("a@b.com", "password123")));

        verifyNoInteractions(encoder, jwtService);
    }

    @Test
    void login_wrongPassword_throws() {
        when(repository.findByEmail("a@b.com")).thenReturn(Optional.of(existingUser()));
        when(encoder.matches("wrong", "HASH")).thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
                () -> service.login(new LoginRequest("a@b.com", "wrong")));

        verifyNoInteractions(jwtService);
    }
}