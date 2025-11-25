package br.com.smartmesquitaapi.auth;

import br.com.smartmesquitaapi.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@Entity
@Getter
@Setter
@RequiredArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    UUID refreshTokenId;

    @Column(name = "token", nullable = false)
    String token;

    @Column(nullable = false)
    Instant expiryDate;

    @ManyToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId")
    User user;
}
