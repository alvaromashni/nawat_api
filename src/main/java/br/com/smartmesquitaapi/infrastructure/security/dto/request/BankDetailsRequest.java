package br.com.smartmesquitaapi.infrastructure.security.dto.request;

import br.com.smartmesquitaapi.domain.user.PixKeyType;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para dados bancários no registro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BankDetailsRequest {

    @NotBlank(message = "Chave PIX é obrigatória")
    private String pixKey;

    private PixKeyType pixKeyType;

    private String bankName;

    private String accountHolder;

    private String cnpj;

    private String bankBranch;

    private String accountNumber;
}

