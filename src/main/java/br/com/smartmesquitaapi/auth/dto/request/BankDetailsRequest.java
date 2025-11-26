package br.com.smartmesquitaapi.auth.dto.request;

import br.com.smartmesquitaapi.user.domain.PixKeyType;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

/**
 * DTO para dados bancários no registro
 */
@Data
@Getter
@Setter
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

