package br.com.smartmesquitaapi.domain.user;

/**
 * Tipos de chave PIX suportados
 */
public enum PixKeyType {
    EMAIL,    // E-mail
    PHONE,    // Telefone celular (+55DDNNNNNNNNN)
    CPF,      // CPF (11 dígitos)
    CNPJ,     // CNPJ (14 dígitos)
    EVP       // Chave aleatória (UUID)
}
