package br.com.smartmesquitaapi.infrastructure.pix;

import br.com.smartmesquitaapi.domain.user.PixKeyType;

import java.util.regex.Pattern;

/**
 * Validador de chaves PIX
 * Valida formato e detecta tipo de chave automaticamente
 */
public class PixKeyValidator {

    // Padrões regex para validação
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+55[1-9]{2}9?[0-9]{8}$"
    );

    private static final Pattern CPF_PATTERN = Pattern.compile("^[0-9]{11}$");
    private static final Pattern CNPJ_PATTERN = Pattern.compile("^[0-9]{14}$");

    private static final Pattern EVP_PATTERN = Pattern.compile(
            "^[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}$",
            Pattern.CASE_INSENSITIVE
    );

    /**
     * Valida se uma chave PIX é válida
     *
     * @param key Chave a ser validada
     * @return true se válida, false caso contrário
     */
    public static boolean isValid(String key){
        if (key == null || key.isBlank()){
            return false;
        }

        String cleanKey = clean(key);
        PixKeyType detectedType = detectType(cleanKey);

        return detectedType != null;
    }

    /**
     * Valida se uma chave corresponde ao tipo especificado
     *
     * @param key Chave a ser validada
     * @param type Tipo esperado
     * @return true se a chave é válida para o tipo, false caso contrário
     */
    public static boolean isValidForType(String key, PixKeyType type){
        if (key == null || key.isBlank() || type == null){
            return false;
        }

        String cleanKey = clean(key);

        return switch (type) {
            case EMAIL -> EMAIL_PATTERN.matcher(cleanKey).matches();
            case PHONE -> PHONE_PATTERN.matcher(cleanKey).matches();
            case CPF -> CPF_PATTERN.matcher(cleanKey).matches() && isValidCpf(cleanKey);
            case CNPJ -> CNPJ_PATTERN.matcher(cleanKey).matches() && isValidCnpj(cleanKey);
            case EVP -> EVP_PATTERN.matcher(cleanKey).matches();
        };
    }
    /**
     * Detecta automaticamente o tipo da chave PIX
     *
     * @param key Chave a ser analisada
     * @return Tipo detectado ou null se inválida
     */
    public static PixKeyType detectType(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        String cleanKey = clean(key);

        // Tenta detectar cada tipo
        if (EMAIL_PATTERN.matcher(cleanKey).matches()) {
            return PixKeyType.EMAIL;
        }
        if (PHONE_PATTERN.matcher(cleanKey).matches()) {
            return PixKeyType.PHONE;
        }
        if (EVP_PATTERN.matcher(cleanKey).matches()) {
            return PixKeyType.EVP;
        }
        if (CPF_PATTERN.matcher(cleanKey).matches() && isValidCpf(cleanKey)) {
            return PixKeyType.CPF;
        }
        if (CNPJ_PATTERN.matcher(cleanKey).matches() && isValidCnpj(cleanKey)) {
            return PixKeyType.CNPJ;
        }

        return null;
    }

    /**
     * Limpa e normaliza uma chave PIX
     * Remove espaços, caracteres especiais (exceto para email/EVP)
     *
     * @param key Chave a ser limpa
     * @return Chave limpa
     */
    public static String clean(String key) {
        if (key == null) {
            return null;
        }

        String cleaned = key.trim();

        // Se parece ser email ou EVP, não remove caracteres especiais
        if (cleaned.contains("@") || cleaned.contains("-")) {
            return cleaned;
        }

        // Remove tudo que não é número ou + (para telefone)
        cleaned = cleaned.replaceAll("[^0-9+]", "");

        return cleaned;
    }

    /**
     * Valida CPF usando algoritmo de dígitos verificadores
     *
     * @param cpf CPF apenas com números (11 dígitos)
     * @return true se válido, false caso contrário
     */
    private static boolean isValidCpf(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (inválido)
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Calcula o primeiro dígito verificador
            int sum = 0;
            for (int i = 0; i < 9; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int firstDigit = 11 - (sum % 11);
            if (firstDigit >= 10) firstDigit = 0;

            if (Character.getNumericValue(cpf.charAt(9)) != firstDigit) {
                return false;
            }

            // Calcula o segundo dígito verificador
            sum = 0;
            for (int i = 0; i < 10; i++) {
                sum += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int secondDigit = 11 - (sum % 11);
            if (secondDigit >= 10) secondDigit = 0;

            return Character.getNumericValue(cpf.charAt(10)) == secondDigit;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida CNPJ usando algoritmo de dígitos verificadores
     *
     * @param cnpj CNPJ apenas com números (14 dígitos)
     * @return true se válido, false caso contrário
     */
    private static boolean isValidCnpj(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return false;
        }

        // Verifica se todos os dígitos são iguais (inválido)
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Calcula o primeiro dígito verificador
            int[] weight1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weight1[i];
            }
            int firstDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

            if (Character.getNumericValue(cnpj.charAt(12)) != firstDigit) {
                return false;
            }

            // Calcula o segundo dígito verificador
            int[] weight2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            sum = 0;
            for (int i = 0; i < 13; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weight2[i];
            }
            int secondDigit = sum % 11 < 2 ? 0 : 11 - (sum % 11);

            return Character.getNumericValue(cnpj.charAt(13)) == secondDigit;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Formata uma chave PIX para exibição (mascara dados sensíveis)
     *
     * @param key Chave a ser formatada
     * @return Chave formatada/mascarada
     */
    public static String formatForDisplay(String key) {
        if (key == null || key.isBlank()) {
            return "";
        }

        PixKeyType type = detectType(key);
        if (type == null) {
            return key;
        }

        String cleanKey = clean(key);

        return switch (type) {
            case EMAIL -> {
                int atIndex = key.indexOf('@');
                if (atIndex > 2) {
                    yield key.substring(0, 2) + "***" + key.substring(atIndex);
                }
                yield key;
            }
            case PHONE -> {
                if (cleanKey.length() >= 13) {
                    yield cleanKey.substring(0, 6) + "****" + cleanKey.substring(cleanKey.length() - 4);
                }
                yield cleanKey;
            }
            case CPF -> {
                if (cleanKey.length() == 11) {
                    yield cleanKey.substring(0, 3) + ".***.***-" + cleanKey.substring(9);
                }
                yield cleanKey;
            }
            case CNPJ -> {
                if (cleanKey.length() == 14) {
                    yield cleanKey.substring(0, 2) + ".***.***/****-" + cleanKey.substring(12);
                }
                yield cleanKey;
            }
            case EVP -> {
                if (key.length() > 8) {
                    yield key.substring(0, 8) + "-****-****-****-************";
                }
                yield key;
            }
        };
    }

    /**
     * Formata uma chave PIX no formato padrão para armazenamento
     * Remove formatação, mantém apenas o essencial
     *
     * @param key Chave a ser normalizada
     * @return Chave normalizada
     */
    public static String normalize(String key) {
        if (key == null || key.isBlank()) {
            return null;
        }

        PixKeyType type = detectType(key);
        if (type == null) {
            return clean(key);
        }

        return switch (type) {
            case EMAIL, EVP -> key.trim().toLowerCase();
            case PHONE, CPF, CNPJ -> clean(key);
        };
    }

}
