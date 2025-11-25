package br.com.smartmesquitaapi.user.service;

import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.User;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

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

    /**
     * Verifica/aprova a chave PIX de um usuário
     * @param userId ID do usuário
     * @param proofUrl URL do comprovante de titularidade (opcional)
     */
    @Transactional
    public void verifyPixKey(UUID userId, String proofUrl) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado: " + userId));

        BankDetails bankDetails = user.getBankDetails();
        if (bankDetails == null) {
            throw new RuntimeException("Usuário não possui dados bancários cadastrados");
        }

        if (bankDetails.getPixKey() == null || bankDetails.getPixKey().isBlank()) {
            throw new RuntimeException("Usuário não possui chave PIX cadastrada");
        }

        // Marcar como verificada
        bankDetails.markAsVerified(proofUrl);
        userRepository.saveAndFlush(user);
    }
}
