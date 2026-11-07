package com.parkgrid.service;

import com.parkgrid.domain.User;
import com.parkgrid.repo.UserRepository;
import com.parkgrid.security.JwtService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Map;

@Service
public class AuthService {
    private final UserRepository users;
    private final JwtService jwt;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthService(UserRepository users, JwtService jwt) {
        this.users = users;
        this.jwt = jwt;
    }

    public Map<String, Object> register(String email, String password) {
        if (users.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email already registered");
        User user = new User(email, encoder.encode(password), User.Role.USER);
        users.save(user);
        return Map.of("token", jwt.issue(user.getEmail(), user.getRole().name()), "role", user.getRole());
    }

    public Map<String, Object> createAdmin(String email, String password) {
        if (users.findByEmail(email).isPresent()) throw new IllegalArgumentException("Email already registered");
        User user = new User(email, encoder.encode(password), User.Role.ADMIN);
        users.save(user);
        return Map.of("token", jwt.issue(user.getEmail(), user.getRole().name()), "role", user.getRole());
    }

    public Map<String, Object> login(String email, String password) {
        User user = users.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));
        if (!encoder.matches(password, user.getPasswordHash())) throw new IllegalArgumentException("Invalid credentials");
        return Map.of("token", jwt.issue(user.getEmail(), user.getRole().name()), "role", user.getRole());
    }
}
