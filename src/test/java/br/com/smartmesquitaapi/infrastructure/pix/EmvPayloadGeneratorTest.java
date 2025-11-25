package br.com.smartmesquitaapi.infrastructure.pix;

import br.com.smartmesquitaapi.pix.infrastructure.EmvPayloadGenerator;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Testes unitários para o EmvPayloadGenerator
 */
class EmvPayloadGeneratorTest {

    @Test
    void shouldGenerateValidPayloadWithAmount() {
        // Arrange
        String pixKey = "contato@mesquita.com";
        String merchantName = "Mesquita Central";
        String merchantCity = "Tubarao";
        String txid = "DOACAO123456";
        Integer amountCents = 5000; // R$ 50,00

        // Act
        String payload = EmvPayloadGenerator.generate(
                pixKey, merchantName, merchantCity, txid, amountCents
        );

        // Assert
        assertNotNull(payload);
        assertTrue(payload.startsWith("00020126")); // Payload Format Indicator
        assertTrue(payload.contains("br.gov.bcb.pix")); // GUI
        assertTrue(payload.contains(pixKey));
        assertTrue(payload.contains("50.00")); // Valor formatado
        assertTrue(payload.length() > 100); // Payload tem tamanho razoável

        // Debug
        System.out.println("=== PAYLOAD GERADO ===");
        System.out.println(payload);
        System.out.println("\n" + EmvPayloadGenerator.debugPayload(payload));
    }

    @Test
    void shouldGenerateValidPayloadWithoutAmount() {
        // Arrange
        String pixKey = "+5548999887766";
        String merchantName = "Mesquita Al-Noor";
        String merchantCity = "Florianopolis";
        String txid = "DOA" + System.currentTimeMillis();

        // Act
        String payload = EmvPayloadGenerator.generate(
                pixKey, merchantName, merchantCity, txid, null
        );

        // Assert
        assertNotNull(payload);
        assertTrue(payload.startsWith("00020126"));
        assertFalse(payload.contains("5402")); // Tag 54 não deve estar presente
        System.out.println("\n=== PAYLOAD SEM VALOR ===");
        System.out.println(payload);
    }

    @Test
    void shouldSanitizeMerchantName() {
        // Arrange
        String pixKey = "12345678901234";
        String merchantName = "Mesquita São José - Fundação"; // Com acentos e hífen
        String merchantCity = "São Paulo";
        String txid = "TXN123";

        // Act
        String payload = EmvPayloadGenerator.generate(
                pixKey, merchantName, merchantCity, txid, 1000
        );

        // Assert
        assertNotNull(payload);
        // Deve ter removido acentos
        assertTrue(payload.contains("Sao"));
        System.out.println("\n=== PAYLOAD COM SANITIZAÇÃO ===");
        System.out.println(EmvPayloadGenerator.debugPayload(payload));
    }

    @Test
    void shouldTruncateLongNames() {
        // Arrange
        String pixKey = "pix@example.com";
        String merchantName = "Mesquita Com Nome Muito Longo Que Deve Ser Truncado"; // > 25 chars
        String merchantCity = "Cidade Com Nome Longo"; // > 15 chars
        String txid = "ABC123";

        // Act
        String payload = EmvPayloadGenerator.generate(
                pixKey, merchantName, merchantCity, txid, null
        );

        // Assert
        assertNotNull(payload);
        System.out.println("\n=== PAYLOAD COM TRUNCAMENTO ===");
        System.out.println(EmvPayloadGenerator.debugPayload(payload));
    }

    @Test
    void shouldThrowExceptionForInvalidInputs() {
        // Assert - Chave PIX vazia
        assertThrows(IllegalArgumentException.class, () -> {
            EmvPayloadGenerator.generate("", "Nome", "Cidade", "TXN1", 1000);
        });

        // Assert - Nome vazio
        assertThrows(IllegalArgumentException.class, () -> {
            EmvPayloadGenerator.generate("pix@example.com", "", "Cidade", "TXN1", 1000);
        });

        // Assert - Cidade vazia
        assertThrows(IllegalArgumentException.class, () -> {
            EmvPayloadGenerator.generate("pix@example.com", "Nome", "", "TXN1", 1000);
        });

        // Assert - Txid vazio
        assertThrows(IllegalArgumentException.class, () -> {
            EmvPayloadGenerator.generate("pix@example.com", "Nome", "Cidade", "", 1000);
        });

        // Assert - Txid muito longo
        assertThrows(IllegalArgumentException.class, () -> {
            EmvPayloadGenerator.generate(
                    "pix@example.com", "Nome", "Cidade",
                    "TXID_MUITO_LONGO_COM_MAIS_DE_25_CARACTERES", 1000
            );
        });
    }

    @Test
    void shouldGenerateDifferentCrcForDifferentPayloads() {
        // Arrange
        String txid1 = "TXN1";
        String txid2 = "TXN2";

        // Act
        String payload1 = EmvPayloadGenerator.generate(
                "pix@test.com", "Teste", "Cidade", txid1, 1000
        );
        String payload2 = EmvPayloadGenerator.generate(
                "pix@test.com", "Teste", "Cidade", txid2, 1000
        );

        // Assert
        assertNotEquals(payload1, payload2);
        // CRC deve ser diferente (últimos 4 caracteres)
        assertNotEquals(
                payload1.substring(payload1.length() - 4),
                payload2.substring(payload2.length() - 4)
        );
    }

    @Test
    void shouldFormatAmountCorrectly() {
        // Testa diferentes valores
        String payload1 = EmvPayloadGenerator.generate(
                "test@test.com", "Test", "City", "TXN", 100 // R$ 1,00
        );
        assertTrue(payload1.contains("1.00"));

        String payload2 = EmvPayloadGenerator.generate(
                "test@test.com", "Test", "City", "TXN", 10050 // R$ 100,50
        );
        assertTrue(payload2.contains("100.50"));

        String payload3 = EmvPayloadGenerator.generate(
                "test@test.com", "Test", "City", "TXN", 999999 // R$ 9.999,99
        );
        assertTrue(payload3.contains("9999.99"));
    }
}

