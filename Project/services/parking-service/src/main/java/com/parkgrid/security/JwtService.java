package com.parkgrid.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtService {
    private final SecretKey key; private final long expiration;
    public JwtService(@Value("${parkgrid.jwt-secret}") String secret,@Value("${parkgrid.jwt-expiration-minutes}") long expiration){
        if(secret.length()<32) throw new IllegalArgumentException("parkgrid.jwt-secret must be at least 32 characters");
        key=Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8)); this.expiration=expiration;
    }
    public String issue(String email,String role){return Jwts.builder().subject(email).claim("role",role).issuedAt(new Date()).expiration(Date.from(Instant.now().plusSeconds(expiration*60))).signWith(key).compact();}
    public Jws<Claims> parse(String token){return Jwts.parser().verifyWith(key).build().parseSignedClaims(token);}
}
