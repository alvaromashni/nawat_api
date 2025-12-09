package br.com.smartmesquitaapi.user.controller;

import br.com.smartmesquitaapi.user.dto.OrganizationProfileDto;
import br.com.smartmesquitaapi.user.dto.NotificationsSettingsDto;
import br.com.smartmesquitaapi.organization.service.OrganizationService;
import br.com.smartmesquitaapi.user.service.NotificationSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/users/me")
public class MeController {

    private final NotificationSettingsService notificationSettingsService;
    private final OrganizationService organizationService;

    public MeController(NotificationSettingsService notificationSettingsService, OrganizationService organizationService) {
        this.notificationSettingsService = notificationSettingsService;
        this.organizationService = organizationService;
    }

    @GetMapping("/notification-settings")
    public ResponseEntity<NotificationsSettingsDto> getNotificationSettings(){
        NotificationsSettingsDto notificationDto = notificationSettingsService.getNotificationSettings();
        return ok().body(notificationDto);
    }

    @PutMapping("/notification-settings")
    public ResponseEntity<Void> updateNotificationSettings(@RequestBody  NotificationsSettingsDto notificationDto){
        notificationSettingsService.updateNotificationSettings(notificationDto);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/organization-profile")
    public ResponseEntity<OrganizationProfileDto> getOrganizationProfile(){
        OrganizationProfileDto organizationProfileDto = organizationService.getOrganizationProfile();
        return ResponseEntity.ok().body(organizationProfileDto);
    }

    @PutMapping("/organization-profile")
    public ResponseEntity<Void> updateOrganizationProfile(@RequestBody OrganizationProfileDto dto){
        organizationService.updateOrganizationProfile(dto);
        return ResponseEntity.ok().build();
    }

}
