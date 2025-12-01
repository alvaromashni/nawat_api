package br.com.smartmesquitaapi.user.dto;

import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.MosqueInfo;
import lombok.Data;

@Data
public class MosqueProfileDto {

    private AddressDto addressDto;
    private NotificationsSettingsDto notificationsSettingsDto;
    private BankDetails bankDetails;
    private MosqueInfoDto mosqueInfoDto;


}
