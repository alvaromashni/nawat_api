package br.com.smartmesquitaapi.user.service;

import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.Notification;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.dto.NotificationsSettingsDto;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;


@Service
public class NotificationSettingsService {

    private final UserRepository userRepository;

    public NotificationSettingsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public NotificationsSettingsDto getNotificationSettings(){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado."));

        Notification notification = user.getNotification();
        NotificationsSettingsDto notificationDto = new NotificationsSettingsDto();

        notificationDto.setDailySummary(notification.isDailySummary());
        notificationDto.setDonationDone(notification.isDonationDone());
        notificationDto.setTotemMaintenance(notification.isTotemMaintenance());

        return notificationDto;
    }

    public void updateNotificationSettings(NotificationsSettingsDto notificationDto){
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        User user = userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email não encontrado."));

        Notification notification = user.getNotification();

        notification.setDailySummary(notificationDto.isDailySummary());
        notification.setDonationDone(notificationDto.isDonationDone());
        notification.setTotemMaintenance(notificationDto.isTotemMaintenance());

        userRepository.save(user);
    }
}
