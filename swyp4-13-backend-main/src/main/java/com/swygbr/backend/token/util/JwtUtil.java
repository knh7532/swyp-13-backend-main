package com.swygbr.backend.token.util;

import java.util.Date;
import java.util.concurrent.TimeUnit;

import org.springframework.http.ResponseCookie;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

import com.swygbr.backend.login.property.JwtProperty;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.Password;

@Component
public class JwtUtil {
    private final JwtProperty jwtProperty;
    private final Password key;

    public JwtUtil(JwtProperty jwtProperty){
        this.jwtProperty = jwtProperty;
        key = Keys.password(jwtProperty.getSecretKey().toCharArray());

    }

    private String createToken(long accessTokenExpirationInSeconds, String id, String issuser) {
        Date tokenCreateTime = new Date();
        Date tokenValidity = new Date(tokenCreateTime.getTime() + TimeUnit.MINUTES.toMillis(accessTokenExpirationInSeconds));
        return Jwts.builder()
            .id(id)
            .issuer(issuser)
            .expiration(tokenValidity)
            .signWith(key)
            .compact();
    }

    public String createAccessToken(String refreshToken) {
        if(this.validateToken(refreshToken)){
            return createToken(this.jwtProperty.getAccessTokenExpirationInSeconds(), this.getUserId(refreshToken).toString(), this.getEmail(refreshToken));
        }
        else{
            throw new IllegalArgumentException();
        }
    }
    public String createAccessTokenCookie(String accessToken){
        return  ResponseCookie.from("accessToken", accessToken)
                    .httpOnly(true)
                    // .secure(true)
                    .maxAge(jwtProperty.getAccessTokenExpirationInSeconds())
                    .build()
                    .toString();
    }

    public String createRefreshToken(Long userId, String email) {
        return createToken(this.jwtProperty.getRefreshTokenExpirationInSeconds(), userId.toString(), email);
    }

    public String createRefreshTokenCookie(String refreshToken){
        return  ResponseCookie.from("refreshToken", refreshToken)
                    .httpOnly(true)
                    // .secure(true)
                    .maxAge(jwtProperty.getRefreshTokenExpirationInSeconds())
                    .build()
                    .toString();
    }

    private Jws<Claims> parseJwtClaims(String token){
        return Jwts.parser()
            .verifyWith(this.key)
            .build()
            .parseSignedClaims(token);
    }

    //jwt 비밀키 불일치, 시간 만료 등 체크
    public boolean validateToken(String token){
        try {
            this.parseJwtClaims(token);
            return true;
        }catch (UnsupportedJwtException  | IllegalArgumentException | JwtException ex) {
            return false;
        }
    }

    public Long getUserId(String token) {
        Jws<Claims> claims = this.parseJwtClaims(token);
        return Long.valueOf(claims.getPayload().getId());
    }

    public String getEmail(String token) {
        Jws<Claims> claims = this.parseJwtClaims(token);
        return claims.getPayload().getIssuer();
    }

    public Date getExpiration(String token){
        Jws<Claims> claims = this.parseJwtClaims(token);
        return claims.getPayload().getExpiration();
    }
}