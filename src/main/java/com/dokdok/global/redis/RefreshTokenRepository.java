package com.dokdok.global.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class RefreshTokenRepository {

    private static final String REFRESH_PREFIX = "refresh:";
    private static final String BLACKLIST_PREFIX = "blacklist:";
    private static final long REFRESH_TTL_SECONDS = 14L * 24 * 60 * 60; // 14일

    private final StringRedisTemplate redisTemplate;

    public void save(Long userId, String token) {
        redisTemplate.opsForValue().set(
                REFRESH_PREFIX + userId, token, REFRESH_TTL_SECONDS, TimeUnit.SECONDS);
    }

    public Optional<String> findByUserId(Long userId) {
        return Optional.ofNullable(redisTemplate.opsForValue().get(REFRESH_PREFIX + userId));
    }

    public void delete(Long userId) {
        redisTemplate.delete(REFRESH_PREFIX + userId);
    }

    public void addToBlacklist(String token, long ttlMillis) {
        redisTemplate.opsForValue().set(
                BLACKLIST_PREFIX + token, "logout", ttlMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}
