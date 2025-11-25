package br.com.smartmesquitaapi.auth.dto.request;

import br.com.smartmesquitaapi.user.domain.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfo {
    private UUID id;
    private String name;
    private String email;
    private UserRole role;
    private Boolean isActive;
    private Boolean hasPixKey;
}
