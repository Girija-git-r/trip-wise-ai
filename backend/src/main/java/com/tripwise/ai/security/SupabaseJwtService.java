package com.tripwise.ai.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Verifies JWTs issued by Supabase Auth (HS256, signed with the project's
 * JWT secret from Supabase dashboard: Settings -> API -> JWT Settings).
 * TripWise never issues its own tokens — Supabase is the sole identity
 * provider; this service only validates what it hands the frontend.
 */
@Service
@Slf4j
public class SupabaseJwtService {

    @Value("${app.supabase.jwt-secret}")
    private String jwtSecret;

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }

    public Optional<SupabaseUserClaims> verify(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

            UUID id = UUID.fromString(claims.getSubject());
            String email = claims.get("email", String.class);

            String name = extractName(claims, email);

            return Optional.of(new SupabaseUserClaims(id, email, name));
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Rejected Supabase token: {}", ex.getMessage());
            return Optional.empty();
        }
    }

    @SuppressWarnings("unchecked")
    private String extractName(Claims claims, String email) {
        Map<String, Object> userMetadata = claims.get("user_metadata", Map.class);
        if (userMetadata != null) {
            Object name = userMetadata.get("name");
            if (name instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return email != null ? email.split("@")[0] : "User";
    }
}
