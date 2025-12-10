package br.com.smartmesquitaapi.organization.dto;


import br.com.smartmesquitaapi.user.dto.AddressDto;
import br.com.smartmesquitaapi.user.dto.BankDetailsDto;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.time.LocalDate;

@Data
@JsonDeserialize(using = OrganizationDtoDeserializer.class)
public abstract class OrganizationDto{

    private String orgName;
    private String phoneNumber;
    private LocalDate foundationDate;
    private String administratorName;
    private String cnpj;
    private String openingHours;

    private BankDetailsDto bankDetails;
    private AddressDto addressDto;

}
