package br.com.smartmesquitaapi.model;

import br.com.smartmesquitaapi.config.crypto.CryptoConverter;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@Embeddable
public class BankDetails implements Serializable {

    @Column(name = "bank_name", length = 100)
    private String bankName;

    @Column(name = "agency_number", length = 20)
    private String agency;

    @Column(name = "account_number", length = 40)
    @Convert(converter = CryptoConverter.class)
    private String accountNumber;

    @Column(name = "pix_key")
    private String pixKey;

    @Override
    public String toString() {
        var account = accountNumber == null ? "null" : mask(accountNumber);
        return "BankDetails{" +
                "bankName='" + bankName + '\'' +
                ", agency='" + agency + '\'' +
                ", accountNumber='" + account + '\'' +
                ", pixKey='" + pixKey +
                '}';
    }

    private String mask(String s) {
        if (s.length() <= 4) return "****";
        return "****" + s.substring(s.length() - 4);
    }

}
