package com.kratee.copilot.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock UserRepository repository;
    @Mock PasswordEncoder encoder;
    @InjectMocks UserService service;

    @Test
    void register_savesUserWithHashedPassword() {
        when(repository.existsByEmail("a@b.com")).thenReturn(false);
        when(encoder.encode("password123")).thenReturn("HASH");
        when(repository.save(any(User.class))).thenAnswer(inv -> {
            User u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserResponse res = service.register(new RegisterRequest("a@b.com", "password123"));

        assertEquals(1L, res.id());
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(repository).save(captor.capture());
        assertEquals("HASH", captor.getValue().getPasswordHash());
    }

    @Test
    void register_duplicateEmail_throwsAndDoesNotSave() {
        when(repository.existsByEmail("a@b.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class,
                () -> service.register(new RegisterRequest("a@b.com", "password123")));

        verify(repository, never()).save(any());
    }
}