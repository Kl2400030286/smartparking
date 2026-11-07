package com.parkgrid.domain;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true) private String email;
    @Column(nullable = false) private String passwordHash;
    @Enumerated(EnumType.STRING) @Column(nullable = false) private Role role = Role.USER;
    @Column(nullable = false) private Instant createdAt = Instant.now();
    protected User() {}
    public User(String email, String passwordHash, Role role) { this.email=email; this.passwordHash=passwordHash; this.role=role; }
    public Long getId(){return id;} public String getEmail(){return email;}
    @com.fasterxml.jackson.annotation.JsonIgnore public String getPasswordHash(){return passwordHash;}
    public Role getRole(){return role;}
    public enum Role { USER, ADMIN }
}
