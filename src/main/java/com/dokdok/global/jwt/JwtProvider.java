package com.dokdok.global.jwt;

import com.dokdok.global.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
@RequiredArgsConstructor
public class JwtProvider {

    private final JwtProperties jwtProperties;
    private SecretKey secretKey;

    @PostConstruct
    public void init() {
        byte[] keyBytes = Base64.getDecoder().decode(jwtProperties.getSecret());
        this.secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(Long userId) {
        return buildToken(userId, "access", jwtProperties.getAccessExpiry());
    }

    public String generateRefreshToken(Long userId) {
        return buildToken(userId, "refresh", jwtProperties.getRefreshExpiry());
    }

    private String buildToken(Long userId, String type, long expiryMillis) {
        Date now = new Date();
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("type", type)
                .issuedAt(now)
                .expiration(new Date(now.getTime() + expiryMillis))
                .signWith(secretKey)
                .compact();
    }

    /**
     * 토큰에서 userId 추출. 파싱 실패 시 ExpiredJwtException, JwtException 그대로 throw.
     */
    public Long extractUserId(String token) {
        return Long.parseLong(getClaims(token).getSubject());
    }

    /**
     * 토큰의 남은 유효시간(ms) 반환. 이미 만료된 경우 0 반환.
     */
    public long getRemainingTtlMillis(String token) {
        try {
            Date expiration = getClaims(token).getExpiration();
            long remaining = expiration.getTime() - System.currentTimeMillis();
            return Math.max(remaining, 0);
        } catch (ExpiredJwtException e) {
            return 0;
        }
    }

    private Claims getClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
