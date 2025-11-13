package br.com.smartmesquitaapi.infrastructure.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record LoginRequest(@Email@NotEmpty(message = "email obrigatório") String email,
                           @NotEmpty(message = "a senha é obrigatória") String password) {}
