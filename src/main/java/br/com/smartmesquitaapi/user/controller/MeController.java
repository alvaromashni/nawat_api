package br.com.smartmesquitaapi.user.controller;

import br.com.smartmesquitaapi.user.dto.OrganizationProfileDto;
import br.com.smartmesquitaapi.user.dto.NotificationsSettingsDto;
import br.com.smartmesquitaapi.user.service.OrganizationService;
import br.com.smartmesquitaapi.user.service.NotificationSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/users/me")
public class MeController {

    private final NotificationSettingsService notificationSettingsService;
    private final OrganizationService mosqueService;

    public MeController(NotificationSettingsService notificationSettingsService, OrganizationService mosqueService) {
        this.notificationSettingsService = notificationSettingsService;
        this.mosqueService = mosqueService;
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

    @GetMapping("/mosque-profile")
    public ResponseEntity<OrganizationProfileDto> getMosqueProfile(){
        OrganizationProfileDto mosqueProfileDto = mosqueService.getMosqueProfile();
        return ResponseEntity.ok().body(mosqueProfileDto);
    }

    @PutMapping("/mosque-profile")
    public ResponseEntity<Void> updateMosqueProfile(@RequestBody OrganizationProfileDto dto){
        mosqueService.updateMosqueProfile(dto);
        return ResponseEntity.ok().build();
    }

}
