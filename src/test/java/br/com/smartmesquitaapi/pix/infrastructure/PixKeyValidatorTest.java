package br.com.smartmesquitaapi.pix.infrastructure;

import br.com.smartmesquitaapi.user.domain.PixKeyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("PixKeyValidator - Testes de Validação de Chaves PIX")
class PixKeyValidatorTest {

    // ==================== TESTES DE VALIDAÇÃO GERAL ====================

    @Test
    @DisplayName("Deve validar chave PIX de email válida")
    void shouldValidateValidEmailPixKey() {
        assertTrue(PixKeyValidator.isValid("joao@example.com"));
        assertTrue(PixKeyValidator.isValid("user.name@domain.com.br"));
        assertTrue(PixKeyValidator.isValid("test+tag@gmail.com"));
    }

    @Test
    @DisplayName("Deve validar chave PIX de telefone válida")
    void shouldValidateValidPhonePixKey() {
        assertTrue(PixKeyValidator.isValid("+5511987654321"));
        assertTrue(PixKeyValidator.isValid("+5521912345678"));
        assertTrue(PixKeyValidator.isValid("+5585998765432"));
    }

    @Test
    @DisplayName("Deve validar chave PIX de CPF válido")
    void shouldValidateValidCpfPixKey() {
        assertTrue(PixKeyValidator.isValid("12345678909")); // CPF válido
        assertTrue(PixKeyValidator.isValid("52998224725")); // CPF válido
    }

    @Test
    @DisplayName("Deve validar chave PIX de CNPJ válido")
    void shouldValidateValidCnpjPixKey() {
        assertTrue(PixKeyValidator.isValid("11222333000181")); // CNPJ válido
        assertTrue(PixKeyValidator.isValid("34028316000103")); // CNPJ válido
    }

    @Test
    @DisplayName("Deve validar chave PIX EVP (aleatória) válida")
    void shouldValidateValidEvpPixKey() {
        assertTrue(PixKeyValidator.isValid("123e4567-e89b-12d3-a456-426614174000"));
        assertTrue(PixKeyValidator.isValid("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    @DisplayName("Deve invalidar chave nula ou vazia")
    void shouldInvalidateNullOrEmptyKey() {
        assertFalse(PixKeyValidator.isValid(null));
        assertFalse(PixKeyValidator.isValid(""));
        assertFalse(PixKeyValidator.isValid("   "));
    }

    @Test
    @DisplayName("Deve invalidar chave em formato desconhecido")
    void shouldInvalidateUnknownFormat() {
        assertFalse(PixKeyValidator.isValid("invalid-key"));
        assertFalse(PixKeyValidator.isValid("12345"));
        assertFalse(PixKeyValidator.isValid("@@@@@"));
    }

    // ==================== TESTES DE VALIDAÇÃO POR TIPO ====================

    @ParameterizedTest
    @ValueSource(strings = {
        "joao@example.com",
        "maria.silva@company.com.br",
        "test+tag@gmail.com",
        "user_123@domain.co"
    })
    @DisplayName("Deve validar emails corretos")
    void shouldValidateCorrectEmails(String email) {
        assertTrue(PixKeyValidator.isValidForType(email, PixKeyType.EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "invalid@",
        "@invalid.com",
        "no-at-sign.com",
        "double@@example.com",
        "spaces in@email.com"
    })
    @DisplayName("Deve invalidar emails incorretos")
    void shouldInvalidateIncorrectEmails(String email) {
        assertFalse(PixKeyValidator.isValidForType(email, PixKeyType.EMAIL));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "+5511987654321",
        "+5521912345678",
        "+5585998765432",
        "+5511912345678"
    })
    @DisplayName("Deve validar telefones corretos (formato +55DDNNNNNNNNN)")
    void shouldValidateCorrectPhones(String phone) {
        assertTrue(PixKeyValidator.isValidForType(phone, PixKeyType.PHONE));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "11987654321",        // Sem +55
        "+55119876543",       // Faltam dígitos
        "+551198765432123",   // Dígitos a mais
        "+5501987654321",     // DDD inválido (01)
        "5511987654321"       // Sem +
    })
    @DisplayName("Deve invalidar telefones incorretos")
    void shouldInvalidateIncorrectPhones(String phone) {
        assertFalse(PixKeyValidator.isValidForType(phone, PixKeyType.PHONE));
    }

    @Test
    @DisplayName("Deve validar CPF válido com dígitos verificadores corretos")
    void shouldValidateValidCpfWithCorrectCheckDigits() {
        assertTrue(PixKeyValidator.isValidForType("12345678909", PixKeyType.CPF));
        assertTrue(PixKeyValidator.isValidForType("52998224725", PixKeyType.CPF));
        assertTrue(PixKeyValidator.isValidForType("11144477735", PixKeyType.CPF));
    }

    @Test
    @DisplayName("Deve invalidar CPF com dígitos verificadores incorretos")
    void shouldInvalidateCpfWithIncorrectCheckDigits() {
        assertFalse(PixKeyValidator.isValidForType("12345678900", PixKeyType.CPF));
        assertFalse(PixKeyValidator.isValidForType("11111111111", PixKeyType.CPF));
    }

    @Test
    @DisplayName("Deve invalidar CPF com todos os dígitos iguais")
    void shouldInvalidateCpfWithAllSameDigits() {
        assertFalse(PixKeyValidator.isValidForType("00000000000", PixKeyType.CPF));
        assertFalse(PixKeyValidator.isValidForType("11111111111", PixKeyType.CPF));
        assertFalse(PixKeyValidator.isValidForType("99999999999", PixKeyType.CPF));
    }

    @Test
    @DisplayName("Deve invalidar CPF com tamanho incorreto")
    void shouldInvalidateCpfWithIncorrectLength() {
        assertFalse(PixKeyValidator.isValidForType("123456789", PixKeyType.CPF));
        assertFalse(PixKeyValidator.isValidForType("123456789012", PixKeyType.CPF));
    }

    @Test
    @DisplayName("Deve validar CNPJ válido com dígitos verificadores corretos")
    void shouldValidateValidCnpjWithCorrectCheckDigits() {
        assertTrue(PixKeyValidator.isValidForType("11222333000181", PixKeyType.CNPJ));
        assertTrue(PixKeyValidator.isValidForType("34028316000103", PixKeyType.CNPJ));
    }

    @Test
    @DisplayName("Deve invalidar CNPJ com dígitos verificadores incorretos")
    void shouldInvalidateCnpjWithIncorrectCheckDigits() {
        assertFalse(PixKeyValidator.isValidForType("11222333000100", PixKeyType.CNPJ));
        assertFalse(PixKeyValidator.isValidForType("12345678000199", PixKeyType.CNPJ));
    }

    @Test
    @DisplayName("Deve invalidar CNPJ com todos os dígitos iguais")
    void shouldInvalidateCnpjWithAllSameDigits() {
        assertFalse(PixKeyValidator.isValidForType("00000000000000", PixKeyType.CNPJ));
        assertFalse(PixKeyValidator.isValidForType("11111111111111", PixKeyType.CNPJ));
    }

    @Test
    @DisplayName("Deve invalidar CNPJ com tamanho incorreto")
    void shouldInvalidateCnpjWithIncorrectLength() {
        assertFalse(PixKeyValidator.isValidForType("1122233300018", PixKeyType.CNPJ));
        assertFalse(PixKeyValidator.isValidForType("112223330001811", PixKeyType.CNPJ));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123e4567-e89b-12d3-a456-426614174000",
        "550e8400-e29b-41d4-a716-446655440000",
        "6ba7b810-9dad-11d1-80b4-00c04fd430c8",
        "A1B2C3D4-E5F6-7890-ABCD-EF1234567890"
    })
    @DisplayName("Deve validar chaves EVP (UUID) corretas")
    void shouldValidateCorrectEvpKeys(String evp) {
        assertTrue(PixKeyValidator.isValidForType(evp, PixKeyType.EVP));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "123e4567-e89b-12d3-a456",           // Formato incompleto
        "123e4567e89b12d3a456426614174000", // Sem hífens
        "xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx", // Caracteres inválidos
        "123e4567-e89b-12d3-a456-426614174000-extra" // Extra caracteres
    })
    @DisplayName("Deve invalidar chaves EVP incorretas")
    void shouldInvalidateIncorrectEvpKeys(String evp) {
        assertFalse(PixKeyValidator.isValidForType(evp, PixKeyType.EVP));
    }

    @Test
    @DisplayName("Deve retornar false quando tipo é nulo")
    void shouldReturnFalseWhenTypeIsNull() {
        assertFalse(PixKeyValidator.isValidForType("joao@example.com", null));
    }

    // ==================== TESTES DE DETECÇÃO AUTOMÁTICA DE TIPO ====================

    @Test
    @DisplayName("Deve detectar tipo EMAIL automaticamente")
    void shouldDetectEmailTypeAutomatically() {
        assertEquals(PixKeyType.EMAIL, PixKeyValidator.detectType("joao@example.com"));
        assertEquals(PixKeyType.EMAIL, PixKeyValidator.detectType("user+tag@domain.com.br"));
    }

    @Test
    @DisplayName("Deve detectar tipo PHONE automaticamente")
    void shouldDetectPhoneTypeAutomatically() {
        assertEquals(PixKeyType.PHONE, PixKeyValidator.detectType("+5511987654321"));
        assertEquals(PixKeyType.PHONE, PixKeyValidator.detectType("+5521912345678"));
    }

    @Test
    @DisplayName("Deve detectar tipo CPF automaticamente")
    void shouldDetectCpfTypeAutomatically() {
        assertEquals(PixKeyType.CPF, PixKeyValidator.detectType("12345678909"));
        assertEquals(PixKeyType.CPF, PixKeyValidator.detectType("52998224725"));
    }

    @Test
    @DisplayName("Deve detectar tipo CNPJ automaticamente")
    void shouldDetectCnpjTypeAutomatically() {
        assertEquals(PixKeyType.CNPJ, PixKeyValidator.detectType("11222333000181"));
        assertEquals(PixKeyType.CNPJ, PixKeyValidator.detectType("34028316000103"));
    }

    @Test
    @DisplayName("Deve detectar tipo EVP automaticamente")
    void shouldDetectEvpTypeAutomatically() {
        assertEquals(PixKeyType.EVP, PixKeyValidator.detectType("123e4567-e89b-12d3-a456-426614174000"));
        assertEquals(PixKeyType.EVP, PixKeyValidator.detectType("550e8400-e29b-41d4-a716-446655440000"));
    }

    @Test
    @DisplayName("Deve retornar null para chave inválida na detecção")
    void shouldReturnNullForInvalidKeyInDetection() {
        assertNull(PixKeyValidator.detectType("invalid-key"));
        assertNull(PixKeyValidator.detectType(null));
        assertNull(PixKeyValidator.detectType(""));
        assertNull(PixKeyValidator.detectType("   "));
    }

    @Test
    @DisplayName("Deve priorizar EMAIL sobre outros tipos na detecção")
    void shouldPrioritizeEmailInDetection() {
        // Email deve ser detectado primeiro
        assertEquals(PixKeyType.EMAIL, PixKeyValidator.detectType("test@example.com"));
    }

    @Test
    @DisplayName("Deve priorizar PHONE sobre CPF/CNPJ na detecção")
    void shouldPrioritizePhoneInDetection() {
        assertEquals(PixKeyType.PHONE, PixKeyValidator.detectType("+5511987654321"));
    }

    @Test
    @DisplayName("Deve priorizar EVP sobre CPF/CNPJ na detecção")
    void shouldPrioritizeEvpInDetection() {
        assertEquals(PixKeyType.EVP, PixKeyValidator.detectType("123e4567-e89b-12d3-a456-426614174000"));
    }

    // ==================== TESTES DE LIMPEZA E NORMALIZAÇÃO ====================

    @Test
    @DisplayName("Deve limpar espaços em branco")
    void shouldCleanWhitespace() {
        assertEquals("12345678909", PixKeyValidator.clean("  12345678909  "));
        assertEquals("12345678909", PixKeyValidator.clean("123 456 789 09"));
    }

    @Test
    @DisplayName("Deve preservar @ em emails durante limpeza")
    void shouldPreserveAtSignInEmailsDuringCleaning() {
        assertEquals("joao@example.com", PixKeyValidator.clean("joao@example.com"));
        assertEquals("test@domain.com", PixKeyValidator.clean("  test@domain.com  "));
    }

    @Test
    @DisplayName("Deve preservar hífens em EVP durante limpeza")
    void shouldPreserveHyphensInEvpDuringCleaning() {
        String evp = "123e4567-e89b-12d3-a456-426614174000";
        assertEquals(evp, PixKeyValidator.clean(evp));
    }

    @Test
    @DisplayName("Deve preservar + em telefones durante limpeza")
    void shouldPreservePlusInPhonesDuringCleaning() {
        assertEquals("+5511987654321", PixKeyValidator.clean("+55 11 98765-4321"));
        assertEquals("+5511987654321", PixKeyValidator.clean("+55(11)98765-4321"));
    }

    @Test
    @DisplayName("Deve remover pontos e traços de CPF/CNPJ")
    void shouldRemoveDotsAndDashesFromCpfCnpj() {
        assertEquals("12345678909", PixKeyValidator.clean("123.456.789-09"));
        assertEquals("11222333000181", PixKeyValidator.clean("11.222.333/0001-81"));
    }

    @Test
    @DisplayName("Deve retornar null para chave nula na limpeza")
    void shouldReturnNullForNullKeyInCleaning() {
        assertNull(PixKeyValidator.clean(null));
    }

    // ==================== TESTES DE FORMATAÇÃO PARA EXIBIÇÃO ====================

    @Test
    @DisplayName("Deve mascarar email para exibição")
    void shouldMaskEmailForDisplay() {
        String masked = PixKeyValidator.formatForDisplay("joao@example.com");
        assertTrue(masked.startsWith("jo"));
        assertTrue(masked.contains("@example.com"));
        assertTrue(masked.contains("***"));
    }

    @Test
    @DisplayName("Deve mascarar telefone para exibição")
    void shouldMaskPhoneForDisplay() {
        String masked = PixKeyValidator.formatForDisplay("+5511987654321");
        assertTrue(masked.startsWith("+55119"));
        assertTrue(masked.contains("****"));
        assertTrue(masked.endsWith("4321"));
    }

    @Test
    @DisplayName("Deve mascarar CPF para exibição")
    void shouldMaskCpfForDisplay() {
        String masked = PixKeyValidator.formatForDisplay("12345678909");
        assertTrue(masked.startsWith("123"));
        assertTrue(masked.contains("***"));
        assertTrue(masked.endsWith("09"));
    }

    @Test
    @DisplayName("Deve mascarar CNPJ para exibição")
    void shouldMaskCnpjForDisplay() {
        String masked = PixKeyValidator.formatForDisplay("11222333000181");
        assertTrue(masked.startsWith("11"));
        assertTrue(masked.contains("***"));
        assertTrue(masked.endsWith("81"));
    }

    @Test
    @DisplayName("Deve mascarar EVP para exibição")
    void shouldMaskEvpForDisplay() {
        String masked = PixKeyValidator.formatForDisplay("123e4567-e89b-12d3-a456-426614174000");
        assertTrue(masked.startsWith("123e4567"));
        assertTrue(masked.contains("****"));
    }

    @Test
    @DisplayName("Deve retornar string vazia para chave nula ou vazia na formatação")
    void shouldReturnEmptyStringForNullOrEmptyKeyInFormatting() {
        assertEquals("", PixKeyValidator.formatForDisplay(null));
        assertEquals("", PixKeyValidator.formatForDisplay(""));
        assertEquals("", PixKeyValidator.formatForDisplay("   "));
    }

    // ==================== TESTES DE NORMALIZAÇÃO ====================

    @Test
    @DisplayName("Deve normalizar email para lowercase")
    void shouldNormalizeEmailToLowercase() {
        assertEquals("joao@example.com", PixKeyValidator.normalize("JOAO@EXAMPLE.COM"));
        assertEquals("test@domain.com", PixKeyValidator.normalize("Test@Domain.COM"));
    }

    @Test
    @DisplayName("Deve normalizar EVP para lowercase")
    void shouldNormalizeEvpToLowercase() {
        assertEquals("123e4567-e89b-12d3-a456-426614174000",
                     PixKeyValidator.normalize("123E4567-E89B-12D3-A456-426614174000"));
    }

    @Test
    @DisplayName("Deve normalizar telefone removendo formatação")
    void shouldNormalizePhoneByRemovingFormatting() {
        assertEquals("+5511987654321", PixKeyValidator.normalize("+55 11 98765-4321"));
        assertEquals("+5511987654321", PixKeyValidator.normalize("+55(11)98765-4321"));
    }

    @Test
    @DisplayName("Deve normalizar CPF removendo formatação")
    void shouldNormalizeCpfByRemovingFormatting() {
        assertEquals("12345678909", PixKeyValidator.normalize("123.456.789-09"));
    }

    @Test
    @DisplayName("Deve normalizar CNPJ removendo formatação")
    void shouldNormalizeCnpjByRemovingFormatting() {
        assertEquals("11222333000181", PixKeyValidator.normalize("11.222.333/0001-81"));
    }

    @Test
    @DisplayName("Deve retornar null para chave nula ou vazia na normalização")
    void shouldReturnNullForNullOrEmptyKeyInNormalization() {
        assertNull(PixKeyValidator.normalize(null));
        assertNull(PixKeyValidator.normalize(""));
        assertNull(PixKeyValidator.normalize("   "));
    }

    @Test
    @DisplayName("Deve normalizar chave inválida usando clean")
    void shouldNormalizeInvalidKeyUsingClean() {
        // Para chaves inválidas, deve usar o método clean
        assertNotNull(PixKeyValidator.normalize("invalid-key-123"));
    }

    // ==================== TESTES EDGE CASES ====================

    @Test
    @DisplayName("Deve lidar com chaves com espaços extras")
    void shouldHandleKeysWithExtraSpaces() {
        assertTrue(PixKeyValidator.isValid("   joao@example.com   "));
        assertTrue(PixKeyValidator.isValid("   +5511987654321   "));
    }

    @Test
    @DisplayName("Deve validar CPF formatado após limpeza")
    void shouldValidateFormattedCpfAfterCleaning() {
        String cpfFormatted = "123.456.789-09";
        String cleaned = PixKeyValidator.clean(cpfFormatted);
        assertTrue(PixKeyValidator.isValidForType(cleaned, PixKeyType.CPF));
    }

    @Test
    @DisplayName("Deve validar CNPJ formatado após limpeza")
    void shouldValidateFormattedCnpjAfterCleaning() {
        String cnpjFormatted = "11.222.333/0001-81";
        String cleaned = PixKeyValidator.clean(cnpjFormatted);
        assertTrue(PixKeyValidator.isValidForType(cleaned, PixKeyType.CNPJ));
    }

    @Test
    @DisplayName("Deve detectar tipo corretamente após limpeza e normalização")
    void shouldDetectTypeCorrectlyAfterCleaningAndNormalization() {
        String dirtyKey = "  123.456.789-09  ";
        String normalized = PixKeyValidator.normalize(dirtyKey);
        assertEquals(PixKeyType.CPF, PixKeyValidator.detectType(normalized));
    }

    @ParameterizedTest
    @CsvSource({
        "joao@example.com, EMAIL",
        "+5511987654321, PHONE",
        "12345678909, CPF",
        "11222333000181, CNPJ",
        "123e4567-e89b-12d3-a456-426614174000, EVP"
    })
    @DisplayName("Deve mapear chaves aos tipos corretos")
    void shouldMapKeysToCorrectTypes(String key, PixKeyType expectedType) {
        assertEquals(expectedType, PixKeyValidator.detectType(key));
    }
}
