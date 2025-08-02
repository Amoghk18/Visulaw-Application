package com.visulaw.auth_provider.domain;

import com.visulaw.auth_provider.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginRequest {

    private String firstname;
    private String lastname;
    private String email;
    private String password;
    private Role role;
}
