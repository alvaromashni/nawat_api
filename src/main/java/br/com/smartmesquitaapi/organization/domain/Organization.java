package br.com.smartmesquitaapi.organization.domain;

import br.com.smartmesquitaapi.user.domain.Address;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.User;
import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;

@Data
@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Organization {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String orgName;
    private String phoneNumber;
    private LocalDate foundationDate;
    private String administratorName;
    private String cnpj;
    private String openingHours;

    @Embedded
    private BankDetails bankDetails;

    @Embedded
    private Address address;

    @OneToOne(mappedBy = "organization", cascade = CascadeType.ALL)
    private User user;

    @Column(nullable = false)
    private boolean isEnabled = true;

    // metodos de validacao

    public boolean hasValidPixKey() {
        return bankDetails != null
                && bankDetails.getPixKey() != null
                && !bankDetails.getPixKey().isBlank()
                && bankDetails.getIsVerified() != null
                && bankDetails.getIsVerified();
    }

    public boolean canReceivePayments() {
        return isEnabled && hasValidPixKey();
    }
}
