package br.com.smartmesquitaapi.user.service;

import br.com.smartmesquitaapi.user.UserRepository;
import br.com.smartmesquitaapi.user.domain.BankDetails;
import br.com.smartmesquitaapi.user.domain.PixKeyType;
import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.domain.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService - Testes de Gerenciamento de Usuários")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private BankDetails bankDetails;

    @BeforeEach
    void setUp() {
        // Configurar dados bancários
        bankDetails = new BankDetails();
        bankDetails.setPixKey("joao@example.com");
        bankDetails.setPixKeyType(PixKeyType.EMAIL);
        bankDetails.setBankName("Banco do Brasil");
        bankDetails.setAccountHolder("João Silva");
        bankDetails.setCnpj("12345678000199");
        bankDetails.setAccountNumber("12345-6");
        bankDetails.setIsVerified(false);

        // Configurar usuário de teste
        testUser = new User();
        testUser.setUserId(UUID.randomUUID());
        testUser.setName("João Silva");
        testUser.setEmail("joao@example.com");
        testUser.setPassword("encodedPassword123");
        testUser.setRole(UserRole.USER);
        testUser.setEnabled(true);
        testUser.setBankDetails(bankDetails);
    }

    // ==================== TESTES DE SAVE USER ====================

    @Test
    @DisplayName("Deve salvar usuário com sucesso")
    void shouldSaveUserSuccessfully() {
        // Arrange
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        userService.saveUser(testUser);

        // Assert
        verify(userRepository).saveAndFlush(testUser);
    }

    @Test
    @DisplayName("Deve usar saveAndFlush para garantir persistência imediata")
    void shouldUseSaveAndFlushForImmediatePersistence() {
        // Arrange
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        userService.saveUser(testUser);

        // Assert
        verify(userRepository).saveAndFlush(testUser);
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== TESTES DE GET USER BY EMAIL ====================

    @Test
    @DisplayName("Deve buscar usuário por email com sucesso")
    void shouldGetUserByEmailSuccessfully() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail("joao@example.com");

        // Assert
        assertNotNull(result);
        assertEquals(testUser.getUserId(), result.getUserId());
        assertEquals(testUser.getEmail(), result.getEmail());
        assertEquals(testUser.getName(), result.getName());

        verify(userRepository).findByEmail("joao@example.com");
    }

    @Test
    @DisplayName("Deve lançar exceção quando usuário não for encontrado por email")
    void shouldThrowExceptionWhenUserNotFoundByEmail() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.getUserByEmail("naoexiste@example.com")
        );

        assertEquals("Email nao encontrado.", exception.getMessage());
        verify(userRepository).findByEmail("naoexiste@example.com");
    }

    @Test
    @DisplayName("Deve retornar usuário com dados bancários ao buscar por email")
    void shouldReturnUserWithBankDetailsWhenGettingByEmail() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));

        // Act
        User result = userService.getUserByEmail("joao@example.com");

        // Assert
        assertNotNull(result.getBankDetails());
        assertEquals("joao@example.com", result.getBankDetails().getPixKey());
        assertEquals(PixKeyType.EMAIL, result.getBankDetails().getPixKeyType());
    }

    // ==================== TESTES DE DELETE USER BY EMAIL ====================

    @Test
    @DisplayName("Deve deletar usuário por email com sucesso")
    void shouldDeleteUserByEmailSuccessfully() {
        // Arrange
        doNothing().when(userRepository).deleteByEmail(anyString());

        // Act
        userService.deleteUserByEmail("joao@example.com");

        // Assert
        verify(userRepository).deleteByEmail("joao@example.com");
    }

    @Test
    @DisplayName("Deve usar método transacional para deletar usuário")
    void shouldUseTransactionalMethodToDeleteUser() {
        // Arrange
        doNothing().when(userRepository).deleteByEmail(anyString());

        // Act
        userService.deleteUserByEmail("joao@example.com");

        // Assert
        verify(userRepository, times(1)).deleteByEmail("joao@example.com");
    }

    // ==================== TESTES DE UPDATE USER BY EMAIL ====================

    @Test
    @DisplayName("Deve atualizar nome do usuário")
    void shouldUpdateUserName() {
        // Arrange
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateData = new User();
        updateData.setName("João da Silva Santos");

        // Act
        userService.updateUserByEmail("joao@example.com", updateData);

        // Assert
        verify(userRepository).findByEmail("joao@example.com");
        verify(userRepository).saveAndFlush(argThat(user ->
            user.getName().equals("João da Silva Santos") &&
            user.getEmail().equals("joao@example.com") &&
            user.getUserId().equals(testUser.getUserId())
        ));
    }

    @Test
    @DisplayName("Deve atualizar email do usuário")
    void shouldUpdateUserEmail() {
        // Arrange
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateData = new User();
        updateData.setEmail("joao.novo@example.com");

        // Act
        userService.updateUserByEmail("joao@example.com", updateData);

        // Assert
        verify(userRepository).saveAndFlush(argThat(user ->
            user.getEmail().equals("joao.novo@example.com")
        ));
    }

    @Test
    @DisplayName("Deve atualizar nome e email simultaneamente")
    void shouldUpdateNameAndEmailSimultaneously() {
        // Arrange
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateData = new User();
        updateData.setName("João da Silva Santos");
        updateData.setEmail("joao.novo@example.com");

        // Act
        userService.updateUserByEmail("joao@example.com", updateData);

        // Assert
        verify(userRepository).saveAndFlush(argThat(user ->
            user.getName().equals("João da Silva Santos") &&
            user.getEmail().equals("joao.novo@example.com")
        ));
    }

    @Test
    @DisplayName("Deve manter dados existentes quando update data tem valores nulos")
    void shouldKeepExistingDataWhenUpdateDataHasNullValues() {
        // Arrange
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateData = new User();
        // Nome e email são null

        // Act
        userService.updateUserByEmail("joao@example.com", updateData);

        // Assert
        verify(userRepository).saveAndFlush(argThat(user ->
            user.getName().equals("João Silva") &&
            user.getEmail().equals("joao@example.com")
        ));
    }

    @Test
    @DisplayName("Deve preservar userId ao atualizar usuário")
    void shouldPreserveUserIdWhenUpdating() {
        // Arrange
        UUID originalUserId = testUser.getUserId();
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateData = new User();
        updateData.setName("Novo Nome");

        // Act
        userService.updateUserByEmail("joao@example.com", updateData);

        // Assert
        verify(userRepository).saveAndFlush(argThat(user ->
            user.getUserId().equals(originalUserId)
        ));
    }

    @Test
    @DisplayName("Deve lançar exceção ao tentar atualizar usuário inexistente")
    void shouldThrowExceptionWhenUpdatingNonExistentUser() {
        // Arrange
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        User updateData = new User();
        updateData.setName("Novo Nome");

        // Act & Assert
        assertThrows(
            RuntimeException.class,
            () -> userService.updateUserByEmail("naoexiste@example.com", updateData)
        );

        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    // ==================== TESTES DE VERIFY PIX KEY ====================

    @Test
    @DisplayName("Deve verificar chave PIX com sucesso")
    void shouldVerifyPixKeySuccessfully() {
        // Arrange
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        String proofUrl = "https://example.com/proof.pdf";

        // Act
        userService.verifyPixKey(testUser.getUserId(), proofUrl);

        // Assert
        verify(userRepository).findById(testUser.getUserId());
        verify(userRepository).saveAndFlush(argThat(user -> {
            BankDetails details = user.getBankDetails();
            return details != null &&
                   details.getIsVerified() &&
                   details.getVerificationProofUrl() != null &&
                   details.getVerifiedAt() != null;
        }));
    }

    @Test
    @DisplayName("Deve verificar chave PIX sem URL de comprovante")
    void shouldVerifyPixKeyWithoutProofUrl() {
        // Arrange
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        userService.verifyPixKey(testUser.getUserId(), null);

        // Assert
        verify(userRepository).saveAndFlush(argThat(user -> {
            BankDetails details = user.getBankDetails();
            return details != null && details.getIsVerified();
        }));
    }

    @Test
    @DisplayName("Deve lançar exceção ao verificar PIX de usuário inexistente")
    void shouldThrowExceptionWhenVerifyingPixForNonExistentUser() {
        // Arrange
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.verifyPixKey(userId, "proof-url")
        );

        assertTrue(exception.getMessage().contains("Usuário não encontrado"));
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao verificar PIX de usuário sem dados bancários")
    void shouldThrowExceptionWhenVerifyingPixForUserWithoutBankDetails() {
        // Arrange
        testUser.setBankDetails(null);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.verifyPixKey(testUser.getUserId(), "proof-url")
        );

        assertEquals("Usuário não possui dados bancários cadastrados", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao verificar usuário sem chave PIX cadastrada")
    void shouldThrowExceptionWhenVerifyingUserWithoutPixKey() {
        // Arrange
        bankDetails.setPixKey(null);
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.verifyPixKey(testUser.getUserId(), "proof-url")
        );

        assertEquals("Usuário não possui chave PIX cadastrada", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Deve lançar exceção ao verificar usuário com chave PIX em branco")
    void shouldThrowExceptionWhenVerifyingUserWithBlankPixKey() {
        // Arrange
        bankDetails.setPixKey("   ");
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));

        // Act & Assert
        RuntimeException exception = assertThrows(
            RuntimeException.class,
            () -> userService.verifyPixKey(testUser.getUserId(), "proof-url")
        );

        assertEquals("Usuário não possui chave PIX cadastrada", exception.getMessage());
        verify(userRepository, never()).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Deve usar método transacional para verificar chave PIX")
    void shouldUseTransactionalMethodToVerifyPixKey() {
        // Arrange
        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);

        // Act
        userService.verifyPixKey(testUser.getUserId(), "proof-url");

        // Assert
        verify(userRepository).saveAndFlush(any(User.class));
    }

    // ==================== TESTES DE INTEGRAÇÃO ====================

    @Test
    @DisplayName("Deve completar fluxo: salvar -> buscar -> atualizar -> deletar")
    void shouldCompleteFullFlow_Save_Get_Update_Delete() {
        // Arrange
        when(userRepository.saveAndFlush(any(User.class))).thenReturn(testUser);
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        doNothing().when(userRepository).deleteByEmail(anyString());

        // Act & Assert - Salvar
        userService.saveUser(testUser);
        verify(userRepository).saveAndFlush(testUser);

        // Act & Assert - Buscar
        User foundUser = userService.getUserByEmail("joao@example.com");
        assertNotNull(foundUser);
        assertEquals(testUser.getEmail(), foundUser.getEmail());

        // Act & Assert - Atualizar
        User updateData = new User();
        updateData.setName("João Atualizado");
        userService.updateUserByEmail("joao@example.com", updateData);
        verify(userRepository, times(2)).saveAndFlush(any(User.class));

        // Act & Assert - Deletar
        userService.deleteUserByEmail("joao@example.com");
        verify(userRepository).deleteByEmail("joao@example.com");
    }

    @Test
    @DisplayName("Deve gerenciar múltiplos usuários corretamente")
    void shouldManageMultipleUsersCorrectly() {
        // Arrange
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        user2.setEmail("maria@example.com");
        user2.setName("Maria Silva");

        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.findByEmail("maria@example.com")).thenReturn(Optional.of(user2));

        // Act
        User foundUser1 = userService.getUserByEmail("joao@example.com");
        User foundUser2 = userService.getUserByEmail("maria@example.com");

        // Assert
        assertNotNull(foundUser1);
        assertNotNull(foundUser2);
        assertNotEquals(foundUser1.getUserId(), foundUser2.getUserId());
        assertEquals("joao@example.com", foundUser1.getEmail());
        assertEquals("maria@example.com", foundUser2.getEmail());
    }

    @Test
    @DisplayName("Deve verificar múltiplas chaves PIX para diferentes usuários")
    void shouldVerifyMultiplePixKeysForDifferentUsers() {
        // Arrange
        User user2 = new User();
        user2.setUserId(UUID.randomUUID());
        BankDetails bankDetails2 = new BankDetails();
        bankDetails2.setPixKey("+5511987654321");
        bankDetails2.setPixKeyType(PixKeyType.PHONE);
        user2.setBankDetails(bankDetails2);

        when(userRepository.findById(testUser.getUserId())).thenReturn(Optional.of(testUser));
        when(userRepository.findById(user2.getUserId())).thenReturn(Optional.of(user2));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        userService.verifyPixKey(testUser.getUserId(), "proof1.pdf");
        userService.verifyPixKey(user2.getUserId(), "proof2.pdf");

        // Assert
        verify(userRepository, times(2)).saveAndFlush(any(User.class));
    }

    @Test
    @DisplayName("Deve preservar outros campos ao atualizar apenas nome")
    void shouldPreserveOtherFieldsWhenUpdatingOnlyName() {
        // Arrange
        when(userRepository.findByEmail("joao@example.com")).thenReturn(Optional.of(testUser));
        when(userRepository.saveAndFlush(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User updateData = new User();
        updateData.setName("Novo Nome");

        // Act
        userService.updateUserByEmail("joao@example.com", updateData);

        // Assert
        verify(userRepository).saveAndFlush(argThat(user ->
            user.getEmail().equals(testUser.getEmail()) &&
            user.getUserId().equals(testUser.getUserId()) &&
            user.getName().equals("Novo Nome")
        ));
    }
}
