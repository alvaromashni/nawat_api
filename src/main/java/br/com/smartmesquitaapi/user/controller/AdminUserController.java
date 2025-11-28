package br.com.smartmesquitaapi.user.controller;

import br.com.smartmesquitaapi.user.domain.User;
import br.com.smartmesquitaapi.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
@RestController
@RequestMapping("/api/v1/admin/")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }


    @PostMapping("/post/user")
    public ResponseEntity<Void> postUser(@Valid @RequestBody User user){
        userService.saveUser(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/get/user")
    public ResponseEntity<User> getUserByEmail(@Valid @RequestParam String email){
        userService.getUserByEmail(email);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/delete/user")
    public ResponseEntity<Void> deleteUserByEmail(@Valid @RequestParam String email){
        userService.deleteUserByEmail(email);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/put/user")
    public ResponseEntity<Void> updateUserByEmail(@Valid @RequestParam String email, @RequestBody User user){
        userService.updateUserByEmail(email, user);
        return ResponseEntity.ok().build();
    }

    /**
     * Verifica/aprova a chave PIX de um usuário
     * Para uso administrativo/desenvolvimento
     */
    @PostMapping("/{userId}/verify-pix")
    public ResponseEntity<String> verifyPixKey(
            @PathVariable UUID userId,
            @RequestParam(required = false) String proofUrl,
            @AuthenticationPrincipal User authenticatedUser
    ) {
        userService.verifyPixKey(userId, proofUrl);
        return ResponseEntity.ok("Chave PIX verificada com sucesso para o usuário " + userId);
    }
}