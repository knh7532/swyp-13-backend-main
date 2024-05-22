package com.swygbr.backend.login.filter;

import java.io.IOException;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.swygbr.backend.login.auth.JwtAuthenticationToken;
import com.swygbr.backend.token.util.JwtUtil;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Component
public class JwtCookieFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;

    private String extractAccessTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals("accessToken")) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String accessToken = extractAccessTokenFromCookie(request);
        if (accessToken != null && jwtUtil.validateToken(accessToken) && SecurityContextHolder.getContext().getAuthentication() == null) {
            Long userId = jwtUtil.getUserId(accessToken);
            String email = jwtUtil.getEmail(accessToken);

            JwtAuthenticationToken authToken = new JwtAuthenticationToken(userId, email, accessToken);
            SecurityContextHolder.getContext().setAuthentication(authToken);
        }

        chain.doFilter(request, response);
    }
}