package br.com.smartmesquitaapi.user.dto;

import br.com.smartmesquitaapi.user.domain.PixKeyType;
import lombok.Data;

@Data
public class BankDetailsDto {
    private String bankName;
    private String agency;
    private String accountNumber;
    private String accountHolder;
    private String pixKey;
    private PixKeyType pixKeyType;
}