package br.com.smartmesquitaapi.organization.dto;


import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.dto.AddressDto;
import lombok.Data;

import java.time.LocalDate;

@Data
public abstract class OrganizationDto{

    private String orgName;
    private String phoneNumber;
    private LocalDate foundationDate;
    private String administratorName;
    private String cnpj;
    private String openingHours;

    private BankDetails bankDetails;
    private AddressDto addressDto;

}
