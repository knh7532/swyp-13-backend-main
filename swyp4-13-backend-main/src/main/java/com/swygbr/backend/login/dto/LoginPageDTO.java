package com.swygbr.backend.login.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class LoginPageDTO {
    private Long id;
    private String email;
    private String platform;
    private String refreshToken;
}
