package br.com.smartmesquitaapi.pix.controller;

import br.com.smartmesquitaapi.user.domain.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * Controller para endpoints de debug/verificação
 */
@RestController
@RequestMapping("/api/debug")
@RequiredArgsConstructor
@Slf4j
public class PixDebugController {


    /**
     * Verifica dados do usuário autenticado (incluindo chave PIX)
     */
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("GET /api/debug/me - Verificando dados do usuário autenticado");

        if (authenticatedUser == null) {
            return ResponseEntity.ok(Map.of(
                    "error", "authenticatedUser is NULL",
                    "message", "Token JWT não está setando o User no SecurityContext"
            ));
        }

        Map<String, Object> response = new HashMap<>();

        // Dados básicos
        response.put("userId", authenticatedUser.getUserId());
        response.put("name", authenticatedUser.getName());
        response.put("email", authenticatedUser.getEmail());
        response.put("role", authenticatedUser.getRole());
        response.put("isActive", authenticatedUser.getIsEnabled());

        // Dados bancários (chave PIX)
        if (authenticatedUser.getBankDetails() != null) {
            Map<String, Object> bankDetails = new HashMap<>();
            bankDetails.put("pixKey", authenticatedUser.getBankDetails().getPixKey());
            bankDetails.put("pixKeyType", authenticatedUser.getBankDetails().getPixKeyType());
            bankDetails.put("bankName", authenticatedUser.getBankDetails().getBankName());
            bankDetails.put("accountHolder", authenticatedUser.getBankDetails().getAccountHolder());
            bankDetails.put("isVerified", authenticatedUser.getBankDetails().getIsVerified());
            bankDetails.put("verifiedAt", authenticatedUser.getBankDetails().getVerifiedAt());

            response.put("bankDetails", bankDetails);
            response.put("hasPixKey", true);
            response.put("hasValidPixKey", authenticatedUser.hasValidPixKey());
            response.put("canReceivePayments", authenticatedUser.canReceivePayments());
        } else {
            response.put("bankDetails", null);
            response.put("hasPixKey", false);
            response.put("hasValidPixKey", false);
            response.put("canReceivePayments", false);
            response.put("warning", "Usuário não tem dados bancários cadastrados");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Verifica especificamente a chave PIX
     */
    @GetMapping("/pix-key")
    public ResponseEntity<Map<String, Object>> checkPixKey(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("GET /api/debug/pix-key - Verificando chave PIX");

        Map<String, Object> response = new HashMap<>();

        if (authenticatedUser == null) {
            response.put("error", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        response.put("userId", authenticatedUser.getUserId());
        response.put("email", authenticatedUser.getEmail());

        if (authenticatedUser.getBankDetails() == null) {
            response.put("status", "NO_BANK_DETAILS");
            response.put("message", "Usuário não tem dados bancários cadastrados");
            response.put("hasPixKey", false);
            response.put("canCreateCharges", false);
            return ResponseEntity.ok(response);
        }

        String pixKey = authenticatedUser.getBankDetails().getPixKey();
        Boolean isVerified = authenticatedUser.getBankDetails().getIsVerified();

        response.put("pixKey", pixKey);
        response.put("pixKeyType", authenticatedUser.getBankDetails().getPixKeyType());
        response.put("isVerified", isVerified);

        if (pixKey == null || pixKey.isBlank()) {
            response.put("status", "NO_PIX_KEY");
            response.put("message", "Chave PIX não cadastrada");
            response.put("hasPixKey", false);
            response.put("canCreateCharges", false);
        } else if (isVerified == null || !isVerified) {
            response.put("status", "NOT_VERIFIED");
            response.put("message", "Chave PIX cadastrada mas não verificada");
            response.put("hasPixKey", true);
            response.put("canCreateCharges", false);
            response.put("warning", "Você precisa verificar a chave PIX antes de receber pagamentos");
        } else {
            response.put("status", "VERIFIED");
            response.put("message", "Chave PIX válida e verificada");
            response.put("hasPixKey", true);
            response.put("canCreateCharges", true);
            response.put("success", "Você pode criar cobranças PIX!");
        }

        return ResponseEntity.ok(response);
    }

    /**
     * Simula verificação da chave PIX (para testes)
     */
    @PostMapping("/verify-pix")
    public ResponseEntity<Map<String, Object>> verifyPixKey(
            @AuthenticationPrincipal User authenticatedUser
    ) {
        log.info("POST /api/debug/verify-pix - Simulando verificação de chave PIX");

        Map<String, Object> response = new HashMap<>();

        if (authenticatedUser == null) {
            response.put("error", "User not authenticated");
            return ResponseEntity.status(401).body(response);
        }

        if (authenticatedUser.getBankDetails() == null ||
                authenticatedUser.getBankDetails().getPixKey() == null) {
            response.put("error", "Usuário não tem chave PIX cadastrada");
            return ResponseEntity.badRequest().body(response);
        }

        response.put("message", "Chave PIX verificada com sucesso (simulação)");
        response.put("pixKey", authenticatedUser.getBankDetails().getPixKey());
        response.put("isVerified", true);
        response.put("warning", "ATENÇÃO: Esta é uma verificação simulada para testes!");

        return ResponseEntity.ok(response);
    }
}