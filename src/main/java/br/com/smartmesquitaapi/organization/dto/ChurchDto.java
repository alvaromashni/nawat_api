package br.com.smartmesquitaapi.organization.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
public class ChurchDto extends OrganizationDto{
    private String priestName;
}
