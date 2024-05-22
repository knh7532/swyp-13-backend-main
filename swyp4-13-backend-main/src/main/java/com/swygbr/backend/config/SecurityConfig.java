package com.swygbr.backend.config;

import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.swygbr.backend.login.filter.JwtCookieFilter;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    private final JwtCookieFilter jwtCookieFilter;

    @Bean
    public BCryptPasswordEncoder encoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Profile("local")
    public WebSecurityCustomizer localConfig() {
        return (web) -> web.ignoring().requestMatchers(PathRequest.toH2Console())
            .requestMatchers("/login/**");
    }

    @Bean
    public SecurityFilterChain configure(final HttpSecurity http) throws Exception {
        return http//.cors(withDefaults())
                .csrf((csrf) -> csrf.disable())
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(httpSecurityFormLoginConfigurer -> httpSecurityFormLoginConfigurer.disable()) // 로그인 폼 미사용
                .httpBasic(httpSecurityHttpBasicConfigurer -> httpSecurityHttpBasicConfigurer.disable()) // http basic 미사용
                .authorizeHttpRequests((authorize) -> authorize
                        .requestMatchers("/login/**", "/token/refresh").permitAll()
                        .requestMatchers(PathRequest.toH2Console()).permitAll() // 허용되는 URL 추가
                        .anyRequest().authenticated())
                .addFilterBefore(jwtCookieFilter, UsernamePasswordAuthenticationFilter.class) // JWT Filter 추가
                .build();
    }

}
