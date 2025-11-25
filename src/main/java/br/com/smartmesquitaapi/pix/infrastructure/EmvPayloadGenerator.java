package br.com.smartmesquitaapi.pix.infrastructure;

import java.nio.charset.StandardCharsets;

/**
 * Gerador de payload EMV (QR Code dinâmico PIX) seguindo a especificação do Banco Central.
 *
 * Referência: Manual de Padrões para Iniciação do Pix - Banco Central do Brasil
 *
 * Formato TLV (Tag-Length-Value):
 * - Tag: identificador do campo (2 dígitos)
 * - Length: tamanho do valor (2 dígitos)
 * - Value: conteúdo do campo
 */
public class EmvPayloadGenerator {

    // Tags principais do payload PIX
    private static final String TAG_PAYLOAD_FORMAT_INDICATOR = "00";
    private static final String TAG_MERCHANT_ACCOUNT_INFORMATION = "26";
    private static final String TAG_MERCHANT_CATEGORY_CODE = "52";
    private static final String TAG_TRANSACTION_CURRENCY = "53";
    private static final String TAG_TRANSACTION_AMOUNT = "54";
    private static final String TAG_COUNTRY_CODE = "58";
    private static final String TAG_MERCHANT_NAME = "59";
    private static final String TAG_MERCHANT_CITY = "60";
    private static final String TAG_ADDITIONAL_DATA_FIELD = "62";
    private static final String TAG_CRC16 = "63";

    // Sub-tags do Merchant Account Information (tag 26)
    private static final String TAG_GUI = "00";
    private static final String TAG_PIX_KEY = "01";

    // Sub-tags do Additional Data Field (tag 62)
    private static final String TAG_TXID = "05";

    // Valores fixos
    private static final String PAYLOAD_FORMAT_INDICATOR_VALUE = "01";
    private static final String GUI_VALUE = "br.gov.bcb.pix"; // Identificador do PIX
    private static final String MERCHANT_CATEGORY_CODE_VALUE = "0000"; // Código genérico
    private static final String TRANSACTION_CURRENCY_VALUE = "986"; // BRL (Real Brasileiro)
    private static final String COUNTRY_CODE_VALUE = "BR";

    /**
     * Gera o payload EMV completo para QR Code PIX estático
     *
     * @param pixKey Chave PIX (email, telefone, CPF, CNPJ ou EVP)
     * @param merchantName Nome do recebedor (máx 25 caracteres)
     * @param merchantCity Cidade do recebedor (máx 15 caracteres)
     * @param txid Transaction ID (máx 25 caracteres, alfanumérico)
     * @param amountCents Valor em centavos (null para valor em aberto)
     * @return Payload EMV completo com CRC-16
     */
    public static String generate(
            String pixKey,
            String merchantName,
            String merchantCity,
            String txid,
            Integer amountCents
    ) {
        validateInputs(pixKey, merchantName, merchantCity, txid);

        StringBuilder payload = new StringBuilder();

        // 00: Payload Format Indicator
        payload.append(buildTlv(TAG_PAYLOAD_FORMAT_INDICATOR, PAYLOAD_FORMAT_INDICATOR_VALUE));

        // 26: Merchant Account Information (contém GUI e chave PIX)
        String merchantAccountInfo = buildMerchantAccountInfo(pixKey);
        payload.append(buildTlv(TAG_MERCHANT_ACCOUNT_INFORMATION, merchantAccountInfo));

        // 52: Merchant Category Code
        payload.append(buildTlv(TAG_MERCHANT_CATEGORY_CODE, MERCHANT_CATEGORY_CODE_VALUE));

        // 53: Transaction Currency (BRL)
        payload.append(buildTlv(TAG_TRANSACTION_CURRENCY, TRANSACTION_CURRENCY_VALUE));

        // 54: Transaction Amount (opcional - se null, valor é aberto)
        if (amountCents != null && amountCents > 0) {
            String amount = formatAmount(amountCents);
            payload.append(buildTlv(TAG_TRANSACTION_AMOUNT, amount));
        }

        // 58: Country Code
        payload.append(buildTlv(TAG_COUNTRY_CODE, COUNTRY_CODE_VALUE));

        // 59: Merchant Name (sanitizado e truncado)
        String sanitizedName = sanitizeAndTruncate(merchantName, 25);
        payload.append(buildTlv(TAG_MERCHANT_NAME, sanitizedName));

        // 60: Merchant City (sanitizado e truncado)
        String sanitizedCity = sanitizeAndTruncate(merchantCity, 15);
        payload.append(buildTlv(TAG_MERCHANT_CITY, sanitizedCity));

        // 62: Additional Data Field Template (contém txid)
        String additionalData = buildAdditionalDataField(txid);
        payload.append(buildTlv(TAG_ADDITIONAL_DATA_FIELD, additionalData));

        // 63: CRC16 (calculado sobre t0do o payload)
        String payloadWithCrcPlaceholder = payload.toString() + TAG_CRC16 + "04";
        String crc = calculateCRC16(payloadWithCrcPlaceholder);
        payload.append(buildTlv(TAG_CRC16, crc));

        return payload.toString();
    }

    /**
     * Constrói um elemento TLV (Tag-Length-Value)
     */
    private static String buildTlv(String tag, String value) {
        int length = value.getBytes(StandardCharsets.UTF_8).length;
        return tag + String.format("%02d", length) + value;
    }

    /**
     * Constrói o Merchant Account Information (tag 26)
     * Contém o GUI (br.gov.bcb.pix) e a chave PIX
     */
    private static String buildMerchantAccountInfo(String pixKey) {
        StringBuilder merchantAccountInfo = new StringBuilder();

        // 00: GUI
        merchantAccountInfo.append(buildTlv(TAG_GUI, GUI_VALUE));

        // 01: PIX Key
        merchantAccountInfo.append(buildTlv(TAG_PIX_KEY, pixKey));

        return merchantAccountInfo.toString();
    }

    /**
     * Constrói o Additional Data Field (tag 62)
     * Contém o Transaction ID (txid)
     */
    private static String buildAdditionalDataField(String txid) {
        // 05: Transaction ID
        String sanitizedTxid = sanitizeAndTruncate(txid, 25);
        return buildTlv(TAG_TXID, sanitizedTxid);
    }

    /**
     * Formata o valor em reais (de centavos para string decimal)
     * Exemplo: 5000 centavos -> "50.00"
     */
    private static String formatAmount(Integer amountCents) {
        double amountInReais = amountCents / 100.0;
        return String.format(java.util.Locale.US,"%.2f", amountInReais);
    }

    /**
     * Calcula o CRC-16/CCITT-FALSE para o payload PIX
     * Polinômio: 0x1021
     * Valor inicial: 0xFFFF
     * XOR final: 0x0000
     */
    private static String calculateCRC16(String payload) {
        byte[] bytes = payload.getBytes(StandardCharsets.UTF_8);
        int crc = 0xFFFF;
        int polynomial = 0x1021;

        for (byte b : bytes) {
            crc ^= (b & 0xFF) << 8;
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ polynomial;
                } else {
                    crc <<= 1;
                }
                crc &= 0xFFFF; // Mantém apenas 16 bits
            }
        }

        return String.format("%04X", crc);
    }

    /**
     * Sanitiza e trunca uma string para uso no payload
     * Remove acentos e caracteres especiais, mantém apenas alfanuméricos e espaços
     */
    private static String sanitizeAndTruncate(String text, int maxLength) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException("Texto não pode ser vazio");
        }

        // Remove acentos
        String sanitized = removeAccents(text);

        // Remove caracteres especiais (mantém apenas letras, números, espaços e hífens)
        sanitized = sanitized.replaceAll("[^a-zA-Z0-9\\s\\-]", "");

        // Trunca se necessário
        if (sanitized.length() > maxLength) {
            sanitized = sanitized.substring(0, maxLength);
        }

        return sanitized.trim();
    }


    private static String removeAccents(String text) {
        return java.text.Normalizer
                .normalize(text, java.text.Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
    }


    private static void validateInputs(String pixKey, String merchantName, String merchantCity, String txid) {
        if (pixKey == null || pixKey.isBlank()) {
            throw new IllegalArgumentException("Chave PIX é obrigatória");
        }
        if (merchantName == null || merchantName.isBlank()) {
            throw new IllegalArgumentException("Nome do recebedor é obrigatório");
        }
        if (merchantCity == null || merchantCity.isBlank()) {
            throw new IllegalArgumentException("Cidade do recebedor é obrigatória");
        }
        if (txid == null || txid.isBlank()) {
            throw new IllegalArgumentException("Transaction ID (txid) é obrigatório");
        }
        if (txid.length() > 25) {
            throw new IllegalArgumentException("Transaction ID não pode ter mais de 25 caracteres");
        }
    }

    /**
     * Mét0do auxiliar para debug: exibe o payload em formato legível
     */
    public static String debugPayload(String payload) {
        StringBuilder debug = new StringBuilder();
        debug.append("=== PAYLOAD PIX DEBUG ===\n");

        int i = 0;
        while (i < payload.length()) {
            if (i + 4 > payload.length()) break;

            String tag = payload.substring(i, i + 2);
            String lengthStr = payload.substring(i + 2, i + 4);
            int length = Integer.parseInt(lengthStr);

            if (i + 4 + length > payload.length()) break;

            String value = payload.substring(i + 4, i + 4 + length);

            debug.append(String.format("Tag %s (len=%02d): %s\n", tag, length, value));

            i += 4 + length;
        }

        return debug.toString();
    }
}