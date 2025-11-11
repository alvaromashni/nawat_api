package br.com.smartmesquitaapi.config;

import lombok.Builder;

import java.util.UUID;

@Builder
public record JWTUserData(UUID userId, String email) {
}

