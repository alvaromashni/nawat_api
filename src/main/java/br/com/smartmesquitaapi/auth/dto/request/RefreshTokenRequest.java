package br.com.smartmesquitaapi.auth.dto.request;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    String token;
}
