package br.com.smartmesquitaapi.user.controller;

import br.com.smartmesquitaapi.user.dto.NotificationsSettingsDto;
import br.com.smartmesquitaapi.user.service.NotificationSettingsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static org.springframework.http.ResponseEntity.ok;

@RestController
@RequestMapping("/api/v1/users/me")
public class MeController {

    private final NotificationSettingsService notificationSettingsService;

    public MeController(NotificationSettingsService notificationSettingsService) {
        this.notificationSettingsService = notificationSettingsService;
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

}
