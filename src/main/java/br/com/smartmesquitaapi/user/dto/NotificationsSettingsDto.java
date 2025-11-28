package br.com.smartmesquitaapi.user.dto;

import lombok.Data;


@Data
public class NotificationsSettingsDto {

    private boolean donationDone;
    private boolean dailySummary;
    private boolean totemMaintenance;

}
