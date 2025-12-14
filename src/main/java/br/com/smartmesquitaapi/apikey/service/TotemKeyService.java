package br.com.smartmesquitaapi.apikey.service;

import br.com.smartmesquitaapi.apikey.domain.TotemKey;
import br.com.smartmesquitaapi.apikey.dto.TotemKeyListResponse;
import br.com.smartmesquitaapi.apikey.dto.TotemKeyResponse;
import br.com.smartmesquitaapi.apikey.repository.TotemKeyRepository;
import br.com.smartmesquitaapi.user.domain.User;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class TotemKeyService {

    private static final Logger logger = LoggerFactory.getLogger(TotemKeyService.class);
    private final TotemKeyRepository totemKeyRepository;

    public TotemKeyService(TotemKeyRepository totemKeyRepository) {
        this.totemKeyRepository = totemKeyRepository;
    }

    @Transactional
    public TotemKeyResponse createTotemKey(String totemName, User adminUser){
        logger.info("=== TotemKeyService.createTotemKey ===");
        logger.info("totemName: {}", totemName);
        logger.info("adminUser: {}", adminUser != null ? adminUser.getEmail() : "NULL");

        if (adminUser.getOrganization() == null){
            logger.error("Usuário {} não possui organização", adminUser.getEmail());
            throw new IllegalArgumentException("Usuário não pertence a uma organização.");
        }

        logger.info("Organização do usuário: {}", adminUser.getOrganization().getOrgName());

        String newApiKey = "totem_" + UUID.randomUUID().toString().replace("-", "");
        logger.info("Chave gerada: {}", newApiKey);

        TotemKey totem = new TotemKey(
                totemName,
                newApiKey,
                adminUser.getOrganization()
        );

        logger.info("Salvando totem no banco de dados...");
        TotemKey savedTotem = totemKeyRepository.save(totem);
        logger.info("Totem salvo com ID: {}", savedTotem.getId());

        return new TotemKeyResponse(savedTotem.getName(), savedTotem.getKeyValue());

    }

    public List<TotemKeyListResponse> getTotemKeys(User user, boolean activeOnly) {
        logger.info("=== TotemKeyService.getTotemKeys ===");
        logger.info("User: {}", user != null ? user.getEmail() : "NULL");
        logger.info("activeOnly: {}", activeOnly);

        if (user.getOrganization() == null){
            logger.error("Usuário {} não possui organização", user.getEmail());
            throw new IllegalArgumentException("Usuário não pertence a uma organização.");
        }

        List<TotemKey> totemKeys;
        if (activeOnly) {
            totemKeys = totemKeyRepository.findByOrganizationAndIsActiveTrueOrderByCreatedAtDesc(user.getOrganization());
        } else {
            totemKeys = totemKeyRepository.findByOrganizationOrderByCreatedAtDesc(user.getOrganization());
        }

        logger.info("Encontradas {} chaves de totem", totemKeys.size());

        return totemKeys.stream()
                .map(this::mapToListResponse)
                .collect(Collectors.toList());
    }

    private TotemKeyListResponse mapToListResponse(TotemKey totemKey) {
        String maskedKey = maskKeyValue(totemKey.getKeyValue());
        return new TotemKeyListResponse(
                totemKey.getId(),
                totemKey.getName(),
                maskedKey,
                totemKey.isActive(),
                totemKey.getCreatedAt()
        );
    }

    private String maskKeyValue(String keyValue) {
        // Mostra apenas os últimos 8 caracteres, mascara o resto
        // Exemplo: totem_abc123xyz456 -> totem_****xyz456
        if (keyValue == null || keyValue.length() <= 14) {
            return keyValue;
        }
        int visibleChars = 8;
        int prefixLength = 6; // "totem_"
        String prefix = keyValue.substring(0, prefixLength);
        String suffix = keyValue.substring(keyValue.length() - visibleChars);
        return prefix + "****" + suffix;
    }
}
