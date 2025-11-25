package br.com.smartmesquitaapi.api.dto;

import br.com.smartmesquitaapi.pix.domain.PixChargeStatus;

public record UpdateStatusRequest(
        PixChargeStatus status,
        String reason
) {}
