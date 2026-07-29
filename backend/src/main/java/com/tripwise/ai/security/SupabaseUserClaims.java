package com.tripwise.ai.security;

import java.util.UUID;

/** Identity extracted from a verified Supabase-issued JWT. */
public record SupabaseUserClaims(UUID id, String email, String name) {
}
