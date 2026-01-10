package com.dokdok.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF 비활성화 (API 서버에서는 일반적으로 비활성화)
                .csrf(AbstractHttpConfigurer::disable)

                // 세션 정책: STATELESS (JWT 사용 시)
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 요청 인증 설정
                .authorizeHttpRequests(auth -> auth
                        // 개발 중인 API 엔드포인트 허용
                        .requestMatchers("/api/**").permitAll()

                        // Swagger UI 허용 (추후 Swagger 설정 시)
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // 헬스체크 엔드포인트 허용
                        .requestMatchers("/actuator/**").permitAll()

                        // 나머지 요청은 인증 필요
                        .anyRequest().authenticated()
                );

        return http.build();
    }
}
