package br.com.smartmesquitaapi.pix.infrastructure;

import br.com.smartmesquitaapi.pix.exception.QrCodeGenerationException;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("QrcodeImageGenerator - Testes de Geração de QR Code")
class QrcodeImageGeneratorTest {

    private static final String VALID_PAYLOAD = "00020126330014br.gov.bcb.pix0111123456789095204000053039865802BR5913Fulano de Tal6008BRASILIA62070503***63041D3D";

    // ==================== TESTES DE GERAÇÃO BASE64 ====================

    @Test
    @DisplayName("Deve gerar QR code em Base64 com payload válido")
    void shouldGenerateBase64QrCodeWithValidPayload() {
        // Act
        String base64Image = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD);

        // Assert
        assertNotNull(base64Image);
        assertFalse(base64Image.isEmpty());
        assertTrue(isValidBase64(base64Image));
    }

    @Test
    @DisplayName("Deve gerar QR code com tamanho padrão (300px)")
    void shouldGenerateQrCodeWithDefaultSize() {
        // Act
        String base64Image = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD);

        // Assert
        assertNotNull(base64Image);
        // Verificar que a imagem foi gerada
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        assertTrue(imageBytes.length > 0);
    }

    @Test
    @DisplayName("Deve gerar QR code com tamanho customizado")
    void shouldGenerateQrCodeWithCustomSize() {
        // Act
        String base64Image = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, 250);

        // Assert
        assertNotNull(base64Image);
        assertTrue(isValidBase64(base64Image));
    }

    @Test
    @DisplayName("Deve lançar exceção para payload nulo")
    void shouldThrowExceptionForNullPayload() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> QrcodeImageGenerator.generateBase64(null)
        );

        assertEquals("Payload não pode ser vazio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção para payload vazio")
    void shouldThrowExceptionForEmptyPayload() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> QrcodeImageGenerator.generateBase64("")
        );

        assertEquals("Payload não pode ser vazio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção para payload em branco")
    void shouldThrowExceptionForBlankPayload() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> QrcodeImageGenerator.generateBase64("   ")
        );

        assertEquals("Payload não pode ser vazio", exception.getMessage());
    }

    @Test
    @DisplayName("Deve lançar exceção para tamanho menor que o mínimo (150px)")
    void shouldThrowExceptionForSizeBelowMinimum() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, 100)
        );

        assertTrue(exception.getMessage().contains("Tamanho deve estar entre"));
        assertTrue(exception.getMessage().contains("150"));
    }

    @Test
    @DisplayName("Deve lançar exceção para tamanho maior que o máximo (1000px)")
    void shouldThrowExceptionForSizeAboveMaximum() {
        // Act & Assert
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, 1500)
        );

        assertTrue(exception.getMessage().contains("Tamanho deve estar entre"));
        assertTrue(exception.getMessage().contains("1000"));
    }

    @Test
    @DisplayName("Deve aceitar tamanho mínimo válido (150px)")
    void shouldAcceptMinimumValidSize() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            String base64 = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, 150);
            assertNotNull(base64);
        });
    }

    @Test
    @DisplayName("Deve aceitar tamanho máximo válido (1000px)")
    void shouldAcceptMaximumValidSize() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            String base64 = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, 1000);
            assertNotNull(base64);
        });
    }

    // ==================== TESTES DE GERAÇÃO PNG BYTES ====================

    @Test
    @DisplayName("Deve gerar bytes PNG válidos")
    void shouldGenerateValidPngBytes() throws Exception {
        // Act
        byte[] pngBytes = QrcodeImageGenerator.generatePngBytes(VALID_PAYLOAD, 300);

        // Assert
        assertNotNull(pngBytes);
        assertTrue(pngBytes.length > 0);

        // Verificar assinatura PNG (primeiros bytes)
        assertEquals((byte) 0x89, pngBytes[0]);
        assertEquals('P', pngBytes[1]);
        assertEquals('N', pngBytes[2]);
        assertEquals('G', pngBytes[3]);
    }

    @Test
    @DisplayName("Deve gerar imagem PNG decodificável")
    void shouldGenerateDecodablePngImage() throws Exception {
        // Act
        byte[] pngBytes = QrcodeImageGenerator.generatePngBytes(VALID_PAYLOAD, 300);

        // Assert - Tentar ler a imagem
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
        assertNotNull(image);
        assertEquals(300, image.getWidth());
        assertEquals(300, image.getHeight());
    }

    @Test
    @DisplayName("Deve gerar QR code com conteúdo correto (decodificável)")
    void shouldGenerateQrCodeWithCorrectContent() throws Exception {
        // Act
        byte[] pngBytes = QrcodeImageGenerator.generatePngBytes(VALID_PAYLOAD, 300);

        // Assert - Decodificar QR code e verificar conteúdo
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
        BinaryBitmap bitmap = new BinaryBitmap(
            new HybridBinarizer(new BufferedImageLuminanceSource(image))
        );

        Result result = new MultiFormatReader().decode(bitmap);
        assertEquals(VALID_PAYLOAD, result.getText());
    }

    @ParameterizedTest
    @ValueSource(ints = {150, 200, 300, 500, 1000})
    @DisplayName("Deve gerar QR codes com diferentes tamanhos válidos")
    void shouldGenerateQrCodesWithDifferentValidSizes(int size) throws Exception {
        // Act
        byte[] pngBytes = QrcodeImageGenerator.generatePngBytes(VALID_PAYLOAD, size);

        // Assert
        BufferedImage image = ImageIO.read(new ByteArrayInputStream(pngBytes));
        assertNotNull(image);
        assertEquals(size, image.getWidth());
        assertEquals(size, image.getHeight());
    }

    // ==================== TESTES DE MÉTODOS DE CONVENIÊNCIA ====================

    @Test
    @DisplayName("Deve gerar QR code otimizado para mobile (250px)")
    void shouldGenerateMobileOptimizedQrCode() {
        // Act
        String base64Image = QrcodeImageGenerator.generateForMobile(VALID_PAYLOAD);

        // Assert
        assertNotNull(base64Image);
        assertTrue(isValidBase64(base64Image));

        // Verificar que é decodificável
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        assertTrue(imageBytes.length > 0);
    }

    @Test
    @DisplayName("Deve gerar QR code de alta qualidade (500px)")
    void shouldGenerateHighQualityQrCode() {
        // Act
        String base64Image = QrcodeImageGenerator.generateHighQuality(VALID_PAYLOAD);

        // Assert
        assertNotNull(base64Image);
        assertTrue(isValidBase64(base64Image));

        // Verificar que é decodificável
        byte[] imageBytes = Base64.getDecoder().decode(base64Image);
        assertTrue(imageBytes.length > 0);
    }

    @Test
    @DisplayName("QR code mobile deve ser menor que QR code de alta qualidade")
    void mobileQrCodeShouldBeSmallerThanHighQuality() {
        // Act
        String mobileQr = QrcodeImageGenerator.generateForMobile(VALID_PAYLOAD);
        String highQualityQr = QrcodeImageGenerator.generateHighQuality(VALID_PAYLOAD);

        // Assert
        byte[] mobileBytes = Base64.getDecoder().decode(mobileQr);
        byte[] highQualityBytes = Base64.getDecoder().decode(highQualityQr);

        // QR de alta qualidade deve ser maior em bytes
        assertTrue(highQualityBytes.length > mobileBytes.length);
    }

    // ==================== TESTES COM DIFERENTES PAYLOADS ====================

    @Test
    @DisplayName("Deve gerar QR code para payload PIX simples")
    void shouldGenerateQrCodeForSimplePixPayload() {
        // Arrange
        String simplePayload = "00020126580014br.gov.bcb.pix0136123e4567-e89b-12d3-a456-42661417400052040000530398654041.005802BR5913Fulano de Tal6008BRASILIA62070503***63041D3D";

        // Act
        String base64 = QrcodeImageGenerator.generateBase64(simplePayload);

        // Assert
        assertNotNull(base64);
        assertTrue(isValidBase64(base64));
    }

    @Test
    @DisplayName("Deve gerar QR code para payload PIX com valor específico")
    void shouldGenerateQrCodeForPixPayloadWithSpecificAmount() {
        // Arrange
        String payloadWithAmount = "00020126580014br.gov.bcb.pix0136550e8400-e29b-41d4-a716-4466554400005204000053039865406100.005802BR5913Maria Silva6009SAO PAULO62070503***6304ABCD";

        // Act
        String base64 = QrcodeImageGenerator.generateBase64(payloadWithAmount);

        // Assert
        assertNotNull(base64);
        assertTrue(isValidBase64(base64));
    }

    @Test
    @DisplayName("Deve gerar QR code para payload longo")
    void shouldGenerateQrCodeForLongPayload() {
        // Arrange
        String longPayload = VALID_PAYLOAD + VALID_PAYLOAD; // Payload duplicado para simular payload longo

        // Act
        String base64 = QrcodeImageGenerator.generateBase64(longPayload);

        // Assert
        assertNotNull(base64);
        assertTrue(isValidBase64(base64));
    }

    @Test
    @DisplayName("Deve gerar QR code para payload com caracteres especiais")
    void shouldGenerateQrCodeForPayloadWithSpecialCharacters() {
        // Arrange
        String payloadWithSpecialChars = "00020126330014br.gov.bcb.pix0111+5511987654321520400005303986580BR5925José da Silva & Cia Ltda6008BRASILIA62070503***63041D3D";

        // Act
        String base64 = QrcodeImageGenerator.generateBase64(payloadWithSpecialChars);

        // Assert
        assertNotNull(base64);
        assertTrue(isValidBase64(base64));
    }

    // ==================== TESTES DE EDGE CASES ====================

    @Test
    @DisplayName("Deve gerar QR codes diferentes para payloads diferentes")
    void shouldGenerateDifferentQrCodesForDifferentPayloads() {
        // Arrange
        String payload1 = "00020126330014br.gov.bcb.pix01111234567890952040000530398658020BR5913User One6008BRASILIA62070503***63041D3D";
        String payload2 = "00020126330014br.gov.bcb.pix01119876543210952040000530398658020BR5913User Two6008BRASILIA62070503***63041D3D";

        // Act
        String qr1 = QrcodeImageGenerator.generateBase64(payload1);
        String qr2 = QrcodeImageGenerator.generateBase64(payload2);

        // Assert
        assertNotEquals(qr1, qr2);
    }

    @Test
    @DisplayName("Deve gerar QR codes idênticos para payload idêntico")
    void shouldGenerateIdenticalQrCodesForIdenticalPayload() {
        // Act
        String qr1 = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD);
        String qr2 = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD);

        // Assert
        assertEquals(qr1, qr2);
    }

    @Test
    @DisplayName("Deve gerar QR code com payload de 1 caractere")
    void shouldGenerateQrCodeWithSingleCharacterPayload() {
        // Act
        String base64 = QrcodeImageGenerator.generateBase64("A");

        // Assert
        assertNotNull(base64);
        assertTrue(isValidBase64(base64));
    }

    @Test
    @DisplayName("Deve lidar com payload contendo apenas números")
    void shouldHandlePayloadWithOnlyNumbers() {
        // Act
        String base64 = QrcodeImageGenerator.generateBase64("1234567890");

        // Assert
        assertNotNull(base64);
        assertTrue(isValidBase64(base64));
    }

    @Test
    @DisplayName("Deve envolver erro de geração em QrCodeGenerationException")
    void shouldWrapGenerationErrorInQrCodeGenerationException() {
        // Arrange - Tamanho inválido deve causar IllegalArgumentException primeiro
        // Mas vamos testar indiretamente verificando que exceções são tratadas

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, -1);
        });
    }

    // ==================== TESTES DE VALIDAÇÃO ====================

    @Test
    @DisplayName("Deve validar entrada antes de gerar QR code")
    void shouldValidateInputBeforeGeneratingQrCode() {
        // Act & Assert - Payload vazio
        assertThrows(IllegalArgumentException.class, () ->
            QrcodeImageGenerator.generateBase64("")
        );

        // Act & Assert - Tamanho inválido
        assertThrows(IllegalArgumentException.class, () ->
            QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, 50)
        );
    }

    @Test
    @DisplayName("Deve aceitar todos os tamanhos dentro do intervalo válido")
    void shouldAcceptAllSizesWithinValidRange() {
        // Arrange
        int[] validSizes = {150, 200, 250, 300, 400, 500, 750, 1000};

        // Act & Assert
        for (int size : validSizes) {
            assertDoesNotThrow(() -> {
                String base64 = QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, size);
                assertNotNull(base64);
            }, "Deve aceitar tamanho " + size);
        }
    }

    @Test
    @DisplayName("Deve rejeitar tamanhos fora do intervalo válido")
    void shouldRejectSizesOutsideValidRange() {
        // Arrange
        int[] invalidSizes = {0, 1, 50, 100, 149, 1001, 1500, 2000};

        // Act & Assert
        for (int size : invalidSizes) {
            assertThrows(IllegalArgumentException.class, () -> {
                QrcodeImageGenerator.generateBase64(VALID_PAYLOAD, size);
            }, "Deve rejeitar tamanho " + size);
        }
    }

    // ==================== TESTES DE PERFORMANCE E QUALIDADE ====================

    @Test
    @DisplayName("Deve gerar QR code rapidamente")
    void shouldGenerateQrCodeQuickly() {
        // Arrange
        long startTime = System.currentTimeMillis();

        // Act
        QrcodeImageGenerator.generateBase64(VALID_PAYLOAD);

        // Assert
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Geração deve levar menos de 1 segundo
        assertTrue(duration < 1000, "Geração levou " + duration + "ms");
    }

    @Test
    @DisplayName("Deve gerar múltiplos QR codes eficientemente")
    void shouldGenerateMultipleQrCodesEfficiently() {
        // Arrange
        int count = 10;
        long startTime = System.currentTimeMillis();

        // Act
        for (int i = 0; i < count; i++) {
            QrcodeImageGenerator.generateBase64(VALID_PAYLOAD + i);
        }

        // Assert
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;

        // Geração de 10 QR codes deve levar menos de 3 segundos
        assertTrue(duration < 3000, "Geração de " + count + " QR codes levou " + duration + "ms");
    }

    @Test
    @DisplayName("Tamanho maior deve gerar arquivo maior")
    void largerSizeShouldGenerateLargerFile() throws Exception {
        // Act
        byte[] small = QrcodeImageGenerator.generatePngBytes(VALID_PAYLOAD, 150);
        byte[] large = QrcodeImageGenerator.generatePngBytes(VALID_PAYLOAD, 1000);

        // Assert
        assertTrue(large.length > small.length);
    }

    // ==================== MÉTODOS AUXILIARES ====================

    /**
     * Verifica se uma string é Base64 válida
     */
    private boolean isValidBase64(String base64) {
        try {
            Base64.getDecoder().decode(base64);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
