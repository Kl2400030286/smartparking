package com.parkgrid.service;

import com.parkgrid.domain.User;
import com.parkgrid.repo.UserRepository;
import com.parkgrid.security.JwtService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {
    @Mock UserRepository users;
    @Mock JwtService jwt;
    @InjectMocks AuthService auth;

    @Test
    void registerCreatesUserRoleAndToken() {
        when(users.findByEmail("user@example.com")).thenReturn(Optional.empty());
        when(jwt.issue("user@example.com", "USER")).thenReturn("token");
        when(users.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var result = auth.register("user@example.com", "password123");

        assertEquals("token", result.get("token"));
        assertEquals(User.Role.USER, result.get("role"));
        verify(users).save(argThat(user -> user.getRole() == User.Role.USER
                && !user.getPasswordHash().equals("password123")));
    }

    @Test
    void duplicateRegistrationIsRejected() {
        when(users.findByEmail("user@example.com"))
                .thenReturn(Optional.of(new User("user@example.com", "hash", User.Role.USER)));

        var exception = assertThrows(IllegalArgumentException.class,
                () -> auth.register("user@example.com", "password123"));

        assertEquals("Email already registered", exception.getMessage());
        verify(users, never()).save(any());
    }

    @Test
    void invalidLoginIsRejected() {
        when(users.findByEmail("user@example.com"))
                .thenReturn(Optional.of(new User("user@example.com", "$2a$10$invalid", User.Role.USER)));

        assertThrows(IllegalArgumentException.class,
                () -> auth.login("user@example.com", "wrong-password"));
        verifyNoInteractions(jwt);
    }
}
