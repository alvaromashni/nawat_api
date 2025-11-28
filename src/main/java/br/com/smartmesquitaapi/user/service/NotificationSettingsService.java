package br.com.smartmesquitaapi.user.service;

import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.dto.NotificationsSettingsDto;
import org.springframework.stereotype.Service;

@Service
public class NotificationSettingsService {

    private UserRepository userRepository;

    public NotificationSettingsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public NotificationsSettingsDto getNotificationSettings(){
        userRepository.
    }
}
