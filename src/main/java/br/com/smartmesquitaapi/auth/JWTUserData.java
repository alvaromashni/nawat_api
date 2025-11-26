package br.com.smartmesquitaapi.auth;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Builder
@Data
@Getter
@Setter
public class JWTUserData {
    private UUID userId;
    private String email;
}

