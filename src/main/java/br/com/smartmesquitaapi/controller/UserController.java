package br.com.smartmesquitaapi.controller;

import br.com.smartmesquitaapi.model.User;
import br.com.smartmesquitaapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
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
}