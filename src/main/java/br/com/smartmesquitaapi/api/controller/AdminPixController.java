package br.com.smartmesquitaapi.api.controller;

import br.com.smartmesquitaapi.api.dto.UpdateStatusRequest;
import br.com.smartmesquitaapi.domain.user.User;
import br.com.smartmesquitaapi.service.pix.PixChargeService;
import br.com.smartmesquitaapi.service.pix.dto.PixChargeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller administrativo para gerenciar cobranças PIX
 * Acesso restrito a ADMIN e STAFF
 */
/**
 * Controller administrativo para gerenciar cobranças PIX
 * Acesso restrito a ADMIN e STAFF
 */
@RestController
@RequestMapping("/api/admin/pix")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminPixController {

    private final PixChargeService pixChargeService;

    /**
     * Força expiração de cobranças pendentes antigas
     */
    @PostMapping("/expire-old-charges")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> expireOldCharges(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("POST /api/admin/pix/expire-old-charges - Admin: {}", authenticatedUser.getUserId());

        int expiredCount = pixChargeService.expireOldCharges();

        return ResponseEntity.ok(Map.of(
                "message", "Cobranças expiradas com sucesso",
                "expiredCount", expiredCount
        ));
    }

    /**
     * Busca cobrança por ID (admin view - mais detalhes)
     */
    @GetMapping("/charges/{chargeId}")
    public ResponseEntity<PixChargeDto> getChargeById(
            @PathVariable UUID chargeId,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.debug("GET /api/admin/pix/charges/{} - User: {}", chargeId, authenticatedUser.getUserId());

        // TODO: Implementar método no service
        // PixChargeDto charge = pixChargeService.getChargeById(chargeId);

        return ResponseEntity.ok().build();
    }

    /**
     * Altera o status de uma cobrança manualmente (uso com cautela)
     */
    @PatchMapping("/charges/{chargeId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PixChargeDto> updateChargeStatus(
            @PathVariable UUID chargeId,
            @RequestBody UpdateStatusRequest request,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.warn("PATCH /api/admin/pix/charges/{}/status - Admin: {} | NewStatus: {} | Reason: {}",
                chargeId,
                authenticatedUser.getUserId(),
                request.status(),
                request.reason()
        );

        // TODO: Implementar método no service
        // PixChargeDto updated = pixChargeService.updateStatus(chargeId, request.status(), request.reason());

        return ResponseEntity.ok().build();
    }

    /**
     * Endpoint para importação de extrato bancário (futuro)
     */
    @PostMapping("/import-extract")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> importExtract(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("POST /api/admin/pix/import-extract - Admin: {}", authenticatedUser.getUserId());

        // TODO: Implementar importação de extrato

        return ResponseEntity.ok(Map.of(
                "message", "Funcionalidade em desenvolvimento"
        ));
    }
}