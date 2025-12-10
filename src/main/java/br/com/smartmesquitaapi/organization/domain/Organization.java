package br.com.smartmesquitaapi.organization.domain;

import br.com.smartmesquitaapi.organization.exception.InvalidCnpjException;
import br.com.smartmesquitaapi.organization.exception.InvalidOrganizationDataException;
import br.com.smartmesquitaapi.user.domain.Address;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Pattern;
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

    @Column(nullable = false)
    private String orgName;

    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$", message = "Número de telefone inválido")
    private String phoneNumber;

    @PastOrPresent(message = "Data de fundação não pode ser no futuro")
    private LocalDate foundationDate;

    @Column(nullable = false)
    private String administratorName;

    @Pattern(regexp = "\\d{14}", message = "CNPJ deve conter 14 dígitos numéricos")
    @Column(unique = true, nullable = false, length = 14)
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

    /**
     * Valida o CNPJ usando o algoritmo de dígito verificador
     * @return true se o CNPJ é válido
     */
    public boolean validateCnpj() {
        if (cnpj == null || !cnpj.matches("\\d{14}")) {
            return false;
        }

        // Verifica CNPJs conhecidos como inválidos
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Calcula o primeiro dígito verificador
            int[] weights1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int sum = 0;
            for (int i = 0; i < 12; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights1[i];
            }
            int digit1 = sum % 11 < 2 ? 0 : 11 - (sum % 11);

            // Calcula o segundo dígito verificador
            int[] weights2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            sum = 0;
            for (int i = 0; i < 13; i++) {
                sum += Character.getNumericValue(cnpj.charAt(i)) * weights2[i];
            }
            int digit2 = sum % 11 < 2 ? 0 : 11 - (sum % 11);

            // Verifica se os dígitos calculados conferem
            return Character.getNumericValue(cnpj.charAt(12)) == digit1
                && Character.getNumericValue(cnpj.charAt(13)) == digit2;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida os dados obrigatórios da organização
     * @throws InvalidOrganizationDataException se algum dado obrigatório estiver inválido
     * @throws InvalidCnpjException se o CNPJ for inválido
     */
    public void validate() {
        if (orgName == null || orgName.isBlank()) {
            throw new InvalidOrganizationDataException("Nome da organização é obrigatório");
        }

        if (administratorName == null || administratorName.isBlank()) {
            throw new InvalidOrganizationDataException("Nome do administrador é obrigatório");
        }

        if (cnpj == null || cnpj.isBlank()) {
            throw new InvalidOrganizationDataException("CNPJ é obrigatório");
        }

        if (!cnpj.matches("\\d{14}")) {
            throw new InvalidCnpjException("CNPJ deve conter exatamente 14 dígitos numéricos");
        }

        if (!validateCnpj()) {
            throw new InvalidCnpjException("CNPJ inválido: " + cnpj);
        }

        if (foundationDate != null && foundationDate.isAfter(LocalDate.now())) {
            throw new InvalidOrganizationDataException("Data de fundação não pode ser no futuro");
        }
    }

    @PrePersist
    @PreUpdate
    protected void onSave() {
        validate();
    }
}
