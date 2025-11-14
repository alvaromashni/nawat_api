package br.com.smartmesquitaapi.api.controller;

import br.com.smartmesquitaapi.domain.user.User;
import br.com.smartmesquitaapi.service.pix.PixChargeService;
import br.com.smartmesquitaapi.service.pix.dto.CreatePixChargeRequest;
import br.com.smartmesquitaapi.service.pix.dto.CreatePixChargeResponse;
import br.com.smartmesquitaapi.service.pix.dto.PixChargeDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/donations")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "PIX Charges", description = "Endpoints para gerenciar cobranças PIX")
@SecurityRequirement(name = "bearer-jwt")
public class PixChargeController {

    private final PixChargeService pixChargeService;

    /**
     * Cria uma cobrança PIX com QR Code
     *
     * @param localId "ID" local da doação (gerado pelo totem)
     * @param request Dados da cobrança
     * @param authenticatedUser "user" autenticado (extraído do JWT)
     * @param httpRequest Request HTTP (para pegar IP)
     * @return Response com QR Code gerado
     */
    @PostMapping("/{localId}/pix")
    @Operation(
            summary = "Criar cobrança PIX",
            description = "Gera um QR Code PIX para doação. Implementa idempotência via idempotencyKey."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "QR Code gerado com sucesso",
                    content = @Content(schema = @Schema(implementation = CreatePixChargeResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Requisição inválida (valor, idempotencyKey, etc)"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "Usuário inativo ou chave PIX não verificada"
            ),
            @ApiResponse(
                    responseCode = "429",
                    description = "Limite de requisições excedido (rate limit)"
            )
    })
    public ResponseEntity<CreatePixChargeResponse> createPixCharge(
            @Parameter(description = "ID local da doação", example = "TOTEM_123")
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

        // Adicionar localId ao request
        request.setLocalDonationId(localId);

        // Criar cobrança
        CreatePixChargeResponse response = pixChargeService.createPixCharge(
                authenticatedUser.getUserId(),
                request,
                getClientIp(httpRequest)
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    /**
     * Consulta uma cobrança PIX pelo localDonationId
     *
     * @param localId ID local da doação
     * @param authenticatedUser Usuário autenticado
     * @return Dados da cobrança
     */
    @GetMapping("/{localId}")
    @Operation(
            summary = "Consultar cobrança PIX",
            description = "Busca uma cobrança pelo ID local (localDonationId) para verificar status"
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
    public ResponseEntity<PixChargeDto> getChargeByLocalId(
            @Parameter(description = "ID local da doação", example = "TOTEM_123")
            @PathVariable String localId,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.debug("GET /api/donations/{} - User: {}", localId, authenticatedUser.getUserId());

        PixChargeDto charge = pixChargeService.getChargeByLocalId(localId);

        // Validar que a cobrança pertence ao usuário autenticado
        // TODO: Implementar verificação de ownership se necessário

        return ResponseEntity.ok(charge);
    }

    /**
     * Busca uma cobrança por Transaction ID (txid)
     *
     * @param txid Transaction ID do PIX
     * @param authenticatedUser Usuário autenticado
     * @return Dados da cobrança
     */
    @GetMapping("/txid/{txid}")
    @Operation(
            summary = "Consultar cobrança por TXID",
            description = "Busca uma cobrança pelo Transaction ID do PIX"
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
    public ResponseEntity<PixChargeDto> getChargeByTxid(
            @Parameter(description = "Transaction ID do PIX", example = "ABC123XYZ")
            @PathVariable String txid,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.debug("GET /api/donations/txid/{} - User: {}", txid, authenticatedUser.getUserId());

        PixChargeDto charge = pixChargeService.getChargeByTxid(txid);

        return ResponseEntity.ok(charge);
    }

    /**
     * Confirma manualmente uma cobrança (staff/admin apenas)
     *
     * @param localId ID local da doação
     * @param request Request com dados da confirmação
     * @param authenticatedUser Usuário autenticado (staff/admin)
     * @return Cobrança confirmada
     */
    @PostMapping("/{localId}/confirm-manual")
    @Operation(
            summary = "Confirmar cobrança manualmente",
            description = "Confirma pagamento via upload de comprovante (staff/admin apenas)"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cobrança confirmada com sucesso",
                    content = @Content(schema = @Schema(implementation = PixChargeDto.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cobrança não encontrada"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Cobrança já foi processada"
            )
    })
    public ResponseEntity<PixChargeDto> confirmManually(
            @Parameter(description = "ID local da doação", example = "TOTEM_123")
            @PathVariable String localId,

            @Valid @RequestBody ConfirmManualRequest request,

            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("POST /api/donations/{}/confirm-manual - ConfirmedBy: {} | ReceiptUrl: {}",
                localId,
                authenticatedUser.getUserId(),
                request.receiptUrl()
        );

        // TODO: Validar se usuário tem permissão (STAFF ou ADMIN)
        // if (!authenticatedUser.getRole().canManageCharges()) {
        //     throw new ForbiddenException("Apenas staff/admin podem confirmar manualmente");
        // }

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
     * DTO para confirmação manual
     */
    @Schema(description = "Request para confirmação manual de pagamento")
    public record ConfirmManualRequest(
            @Schema(description = "URL do comprovante de pagamento", example = "https://storage.example.com/receipt.jpg")
            String receiptUrl,

            @Schema(description = "Observações sobre a confirmação", example = "Pagamento via app Banco X")
            String notes
    ) {}

}
