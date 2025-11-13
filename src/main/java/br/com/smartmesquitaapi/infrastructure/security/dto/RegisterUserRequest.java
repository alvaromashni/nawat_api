package br.com.smartmesquitaapi.infrastructure.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotEmpty;

public record RegisterUserRequest(@NotEmpty(message = "Nome obrigatorio") String name,
                                  @Email @NotEmpty(message = "Email obrigatorio") String email,
                                  @NotEmpty(message = "Senha obrigatoria") String password) {
}
