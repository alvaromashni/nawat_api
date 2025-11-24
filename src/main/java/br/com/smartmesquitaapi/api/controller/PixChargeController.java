package br.com.smartmesquitaapi.api.controller;

import br.com.smartmesquitaapi.domain.user.User;
import br.com.smartmesquitaapi.infrastructure.ratelimit.RateLimitType;
import br.com.smartmesquitaapi.infrastructure.ratelimit.annotations.RateLimit;
import br.com.smartmesquitaapi.service.pix.PixChargeService;
import br.com.smartmesquitaapi.service.pix.dto.CreatePixChargeRequest;
import br.com.smartmesquitaapi.service.pix.dto.CreatePixChargeResponse;
import br.com.smartmesquitaapi.service.pix.dto.PixChargeDto;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.TimeUnit;

/**
 * Controller para gerenciar cobranças PIX
 */
@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Slf4j
public class PixChargeController {

    private final PixChargeService pixChargeService;

    /**
     * Cria uma nova cobrança PIX com QR Code
     */
    @PostMapping("/{localId}/pix")
    @RateLimit(limit = 1, duration = 10, unit = TimeUnit.SECONDS, type = RateLimitType.USER)
    public ResponseEntity<CreatePixChargeResponse> createPixCharge(
            @PathVariable String localId,
            @Valid @RequestBody CreatePixChargeRequest request,
            @AuthenticationPrincipal User authenticatedUser,
            HttpServletRequest httpRequest
    ) {
        log.info("POST /api/donations/{}/pix - User: {} | Amount: R$ {} | IP: {}",
                localId,
                authenticatedUser.getUserId(),
                request.getAmountCents() / 100.0,
                getClientIp(httpRequest)
        );

        request.setLocalDonationId(localId);

        CreatePixChargeResponse response = pixChargeService.createPixCharge(
                authenticatedUser.getUserId(),
                request,
                getClientIp(httpRequest)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * Consulta uma cobrança PIX pelo localDonationId
     */
    @GetMapping("/{localId}")
    public ResponseEntity<PixChargeDto> getChargeByLocalId(
            @PathVariable String localId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.debug("GET /api/donations/{} - User: {}", localId, authenticatedUser.getUserId());

        PixChargeDto charge = pixChargeService.getChargeByLocalId(localId);

        return ResponseEntity.ok(charge);
    }

    /**
     * Busca uma cobrança por Transaction ID (txid)
     */
    @GetMapping("/txid/{txid}")
    public ResponseEntity<PixChargeDto> getChargeByTxid(
            @PathVariable String txid,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.debug("GET /api/donations/txid/{} - User: {}", txid, authenticatedUser.getUserId());

        PixChargeDto charge = pixChargeService.getChargeByTxid(txid);

        return ResponseEntity.ok(charge);
    }

    /**
     * Confirma manualmente uma cobrança (staff/admin apenas)
     */
    @PostMapping("/{localId}/confirm-manual")
    public ResponseEntity<PixChargeDto> confirmManually(
            @PathVariable String localId,
            @Valid @RequestBody ConfirmManualRequest request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("POST /api/donations/{}/confirm-manual - ConfirmedBy: {} | ReceiptUrl: {}",
                localId,
                authenticatedUser.getUserId(),
                request.receiptUrl()
        );

        PixChargeDto confirmed = pixChargeService.confirmManually(
                localId,
                authenticatedUser.getUserId(),
                request.receiptUrl(),
                request.notes()
        );

        return ResponseEntity.ok(confirmed);
    }

    /**
     * Extrai o IP real do cliente (considerando proxies)
     */
    private String getClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }

        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }

        return request.getRemoteAddr();
    }

    /**
     * DTO para confirmação manual (record Java)
     */
    public record ConfirmManualRequest(
            String receiptUrl,
            String notes
    ) {}
}