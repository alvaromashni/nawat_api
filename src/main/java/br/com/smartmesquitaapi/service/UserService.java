package br.com.smartmesquitaapi.service;

import br.com.smartmesquitaapi.model.user.User;
import br.com.smartmesquitaapi.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void saveUser(User user){
        userRepository.saveAndFlush(user);
    }

    public User getUserByEmail(String email){
        return (User) userRepository.findByEmail(email).orElseThrow(
                () -> new RuntimeException("Email nao encontrado.")
        );
    }

    @Transactional
    public void deleteUserByEmail(String email){
        userRepository.deleteByEmail(email);
    }

    public void updateUserByEmail(String email, User user){
        User userEntity = getUserByEmail(email);
        User userUpdated = User.builder()
                .email(user.getEmail() != null ? user.getEmail() : userEntity.getEmail())
                .name(user.getName() != null ? user.getName() : userEntity.getName())
                .userId(userEntity.getUserId())
                .build();

        userRepository.saveAndFlush(userUpdated);
    }
}
