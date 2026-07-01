package com.example.classroomservice.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.List;

@Service
public class JwtService {
    private final SecretKey signingKey;

    public JwtService(@Value("${security.jwt.secret-key}") String secret) {
        this.signingKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public AuthenticatedUser parseUser(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        if (claims.getExpiration() == null || claims.getExpiration().before(new Date())) {
            throw new IllegalArgumentException("Token expired");
        }

        String email = claims.getSubject();
        List<?> rawRoles = claims.get("roles", List.class);
        List<SimpleGrantedAuthority> authorities = rawRoles == null
                ? List.of()
                : rawRoles.stream()
                        .map(String::valueOf)
                        .map(SimpleGrantedAuthority::new)
                        .toList();
        return new AuthenticatedUser(email, authorities);
    }
}
