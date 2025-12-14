package br.com.smartmesquitaapi.apikey;

import br.com.smartmesquitaapi.apikey.dto.TotemKeyListResponse;
import br.com.smartmesquitaapi.apikey.dto.TotemKeyRequest;
import br.com.smartmesquitaapi.apikey.dto.TotemKeyResponse;
import br.com.smartmesquitaapi.apikey.service.TotemKeyService;
import br.com.smartmesquitaapi.user.domain.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/organizations/me/totems")
public class TotemController {

    private static final Logger logger = LoggerFactory.getLogger(TotemController.class);
    private final TotemKeyService totemService;

    public TotemController(TotemKeyService totemService) {
        this.totemService = totemService;
    }

    @PostMapping
    public ResponseEntity<?> createTotemKey(@AuthenticationPrincipal User user, @RequestBody TotemKeyRequest request){
        try {
            logger.info("=== Recebendo requisição para criar totem key ===");
            logger.info("User: {}", user != null ? user.getEmail() : "NULL");
            logger.info("Request name: {}", request != null ? request.name() : "NULL REQUEST");

            if (user == null) {
                logger.error("User é null - problema de autenticação");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
            }

            if (request == null || request.name() == null || request.name().trim().isEmpty()) {
                logger.error("Request inválido: {}", request);
                return ResponseEntity.badRequest().body("Nome do totem é obrigatório");
            }

            logger.info("Chamando service para criar totem key...");
            TotemKeyResponse response = totemService.createTotemKey(request.name(), user);
            logger.info("Totem key criada com sucesso: {}", response.getName());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao criar totem key: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao criar totem key", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao criar chave de totem: " + e.getMessage());
        }
    }

    @GetMapping
    public ResponseEntity<?> getTotemKeys(
            @AuthenticationPrincipal User user,
            @RequestParam(defaultValue = "true") boolean activeOnly) {
        try {
            logger.info("=== Recebendo requisição para listar totem keys ===");
            logger.info("User: {}", user != null ? user.getEmail() : "NULL");
            logger.info("activeOnly: {}", activeOnly);

            if (user == null) {
                logger.error("User é null - problema de autenticação");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Usuário não autenticado");
            }

            logger.info("Chamando service para buscar totem keys...");
            List<TotemKeyListResponse> totemKeys = totemService.getTotemKeys(user, activeOnly);
            logger.info("Retornando {} totem keys", totemKeys.size());

            return ResponseEntity.ok(totemKeys);

        } catch (IllegalArgumentException e) {
            logger.error("Erro de validação ao buscar totem keys: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            logger.error("Erro inesperado ao buscar totem keys", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("Erro ao buscar chaves de totem: " + e.getMessage());
        }
    }

}
