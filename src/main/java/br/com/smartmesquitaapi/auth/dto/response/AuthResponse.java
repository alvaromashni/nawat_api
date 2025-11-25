package br.com.smartmesquitaapi.auth.dto.response;

import br.com.smartmesquitaapi.auth.dto.request.UserInfo;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * DTO de resposta após login/registro
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponse {

    private String token;

    private String refreshToken;

    private String type = "Bearer";

    private UserInfo user;

}
