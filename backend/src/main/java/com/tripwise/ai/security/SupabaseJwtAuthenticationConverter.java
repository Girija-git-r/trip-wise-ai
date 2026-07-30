package com.tripwise.ai.security;

import com.tripwise.ai.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Turns a Spring-Security-verified Supabase {@link Jwt} into the app's own
 * {@link SupabaseUserClaims} principal, and syncs the local profile row from
 * its claims on every request (see {@link UserService#syncFromToken}) so
 * there's no separate "create profile after signup" step.
 */
@Component
@RequiredArgsConstructor
public class SupabaseJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private final UserService userService;

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        UUID id = UUID.fromString(jwt.getSubject());
        String email = jwt.getClaimAsString("email");
        String name = extractName(jwt, email);

        SupabaseUserClaims claims = new SupabaseUserClaims(id, email, name);
        userService.syncFromToken(claims);

        return new UsernamePasswordAuthenticationToken(
                claims, jwt, List.of(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private String extractName(Jwt jwt, String email) {
        Map<String, Object> userMetadata = jwt.getClaimAsMap("user_metadata");
        if (userMetadata != null) {
            Object name = userMetadata.get("name");
            if (name instanceof String s && !s.isBlank()) {
                return s;
            }
        }
        return email != null ? email.split("@")[0] : "User";
    }
}
