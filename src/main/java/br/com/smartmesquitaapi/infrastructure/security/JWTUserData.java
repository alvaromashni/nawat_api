package br.com.smartmesquitaapi.infrastructure.security;

import lombok.Builder;

import java.util.UUID;

@Builder
public record JWTUserData(UUID userId, String email) {
}

