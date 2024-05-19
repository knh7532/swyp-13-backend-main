package com.swygbr.backend.DTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserDto {
    private Long id;
    private String email;
    private String platform;
    private String refreshToken;
}
