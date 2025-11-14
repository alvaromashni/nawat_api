package br.com.smartmesquitaapi.api.controller;

import br.com.smartmesquitaapi.domain.pix.PixChargeStatus;
import br.com.smartmesquitaapi.domain.user.User;
import br.com.smartmesquitaapi.service.pix.PixChargeService;
import br.com.smartmesquitaapi.service.pix.dto.PixChargeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RestController
@RequestMapping("/api/admin/pix")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Admin PIX", description = "Endpoints administrativos para gestão de cobranças PIX")
@SecurityRequirement(name = "bearer-jwt")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class AdminPixController {

    private final PixChargeService pixChargeService;

    /**
     * Força expiração de cobranças pendentes antigas
     *
     * @param authenticatedUser Admin/Staff autenticado
     * @return Número de cobranças expiradas
     */
    @PostMapping("/expire-old-charges")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Expirar cobranças antigas",
            description = "Força a expiração de cobranças pendentes que já passaram do prazo (Admin apenas)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cobranças expiradas com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado (requer role ADMIN)"
            )
    })
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
     *
     * @param chargeId ID da cobrança
     * @param authenticatedUser Admin/Staff autenticado
     * @return Dados completos da cobrança
     */
    @GetMapping("/charges/{chargeId}")
    @Operation(
            summary = "Buscar cobrança por ID",
            description = "Retorna detalhes completos de uma cobrança (Admin/Staff)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cobrança encontrada",
                    content = @Content(schema = @Schema(implementation = PixChargeDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cobrança não encontrada"
            )
    })
    public ResponseEntity<PixChargeDto> getChargeById(
            @Parameter(description = "ID da cobrança (UUID)")
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
     *
     * @param chargeId ID da cobrança
     * @param request Novo status e observações
     * @param authenticatedUser Admin autenticado
     * @return Cobrança atualizada
     */
    @PatchMapping("/charges/{chargeId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(
            summary = "Alterar status da cobrança",
            description = "Altera manualmente o status de uma cobrança (Admin apenas - usar com cautela)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Status alterado com sucesso"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Acesso negado (requer role ADMIN)"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cobrança não encontrada"
            )
    })
    public ResponseEntity<PixChargeDto> updateChargeStatus(
            @Parameter(description = "ID da cobrança (UUID)")
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
    @Operation(
            summary = "Importar extrato bancário",
            description = "Importa extrato (CSV/OFX) para reconciliação automática (Admin apenas)"
    )
    public ResponseEntity<Map<String, Object>> importExtract(
            @AuthenticationPrincipal User authenticatedUser
            // TODO: Adicionar @RequestParam MultipartFile file
    ) {
        log.info("POST /api/admin/pix/import-extract - Admin: {}", authenticatedUser.getUserId());

        // TODO: Implementar importação de extrato

        return ResponseEntity.ok(Map.of(
                "message", "Funcionalidade em desenvolvimento"
        ));
    }

    /**
     * DTO para atualização de status
     */
    @Schema(description = "Request para atualização de status")
    public record UpdateStatusRequest(
            @Schema(description = "Novo status da cobrança")
            PixChargeStatus status,

            @Schema(description = "Motivo da alteração", example = "Pagamento confirmado via telefone")
            String reason
    ) {}
}