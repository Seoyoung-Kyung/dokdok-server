package com.dokdok.user.service;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.jwt.JwtProvider;
import com.dokdok.global.redis.RefreshTokenRepository;
import com.dokdok.user.dto.response.AuthTokens;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    /**
     * Refresh Token 검증 후 새 Access Token + Refresh Token 발급 (Rotation).
     */
    public AuthTokens issueToken(String refreshToken) {
        Long userId;
        try {
            userId = jwtProvider.extractUserId(refreshToken);
        } catch (ExpiredJwtException e) {
            throw new GlobalException(GlobalErrorCode.EXPIRED_TOKEN);
        } catch (JwtException e) {
            throw new GlobalException(GlobalErrorCode.INVALID_TOKEN);
        }

        String storedToken = refreshTokenRepository.findByUserId(userId)
                .orElseThrow(() -> new GlobalException(GlobalErrorCode.REFRESH_TOKEN_NOT_FOUND));

        if (!storedToken.equals(refreshToken)) {
            throw new GlobalException(GlobalErrorCode.INVALID_TOKEN);
        }

        String newAccessToken = jwtProvider.generateAccessToken(userId);
        String newRefreshToken = jwtProvider.generateRefreshToken(userId);
        refreshTokenRepository.save(userId, newRefreshToken);

        log.info("토큰 재발급 완료: userId={}", userId);
        return new AuthTokens(newAccessToken, newRefreshToken);
    }

    /**
     * 로그아웃: Redis Refresh Token 삭제 + Access Token 블랙리스트 등록.
     */
    public void logout(String accessToken, Long userId) {
        refreshTokenRepository.delete(userId);

        if (accessToken != null) {
            long remainingTtl = jwtProvider.getRemainingTtlMillis(accessToken);
            if (remainingTtl > 0) {
                refreshTokenRepository.addToBlacklist(accessToken, remainingTtl);
            }
        }

        log.info("로그아웃 완료: userId={}", userId);
    }
}
