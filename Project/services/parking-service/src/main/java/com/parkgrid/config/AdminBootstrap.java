package com.parkgrid.config;

import com.parkgrid.repo.UserRepository;
import com.parkgrid.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AdminBootstrap {
    @Bean
    CommandLineRunner bootstrapAdmin(
            AuthService auth,
            UserRepository users,
            @Value("${parkgrid.admin-bootstrap-enabled:false}") boolean enabled,
            @Value("${parkgrid.admin-bootstrap-email:}") String email,
            @Value("${parkgrid.admin-bootstrap-password:}") String password) {
        return args -> {
            if (!enabled) {
                return;
            }
            if (email.isBlank() || password.length() < 8) {
                throw new IllegalStateException(
                        "Admin bootstrap requires ADMIN_BOOTSTRAP_EMAIL and an ADMIN_BOOTSTRAP_PASSWORD of at least 8 characters");
            }
            if (users.findByEmail(email).isEmpty()) {
                auth.createAdmin(email, password);
            }
        };
    }
}
