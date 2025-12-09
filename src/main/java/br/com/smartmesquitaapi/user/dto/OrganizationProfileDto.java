package br.com.smartmesquitaapi.user.dto;

import br.com.smartmesquitaapi.organization.dto.OrganizationDto;
import lombok.Data;

@Data
public class OrganizationProfileDto {

    private OrganizationDto organizationDto;
    private NotificationsSettingsDto notificationsSettingsDto;

}
