package br.com.smartmesquitaapi.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

public record CreateDonationRequest(
        @NotNull @Positive BigDecimal value,
        @NotNull String tokenCard,
        @NotNull Long idInstitution
) {}
