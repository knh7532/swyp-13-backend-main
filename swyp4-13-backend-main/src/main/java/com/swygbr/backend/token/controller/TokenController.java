package com.swygbr.backend.token.controller;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.swygbr.backend.token.util.JwtUtil;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RequestMapping("/token")
@RestController
public class TokenController {
    private final JwtUtil jwtUtil;


    @GetMapping("/valid-access-token/")
    public ResponseEntity<?> validAccessToken(@CookieValue(name = "accessToken") String accessToken) {
        if(jwtUtil.validateToken(accessToken)){
            return ResponseEntity
                .ok()
                .build();
        }
        else{
            return ResponseEntity
                .badRequest()
                .build();
        }
    }

    @GetMapping("/refresh")
    public ResponseEntity<?> refreshCookie(@CookieValue(name = "refreshToken") String refreshToken) {
        if(jwtUtil.validateToken(refreshToken)){
            String accessToken = jwtUtil.createAccessToken(refreshToken);
            String accessTokenCookie = jwtUtil.createAccessTokenCookie(accessToken);
            return ResponseEntity
                .ok()
                .header(HttpHeaders.SET_COOKIE, accessTokenCookie.toString())
                .build();
        }
        else{
            return ResponseEntity
                .badRequest()
                .build();
        }
    }
}
