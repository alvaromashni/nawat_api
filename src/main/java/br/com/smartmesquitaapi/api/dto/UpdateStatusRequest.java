package br.com.smartmesquitaapi.api.dto;

import br.com.smartmesquitaapi.domain.pix.PixChargeStatus;

public record UpdateStatusRequest(
        PixChargeStatus status,
        String reason
) {}
