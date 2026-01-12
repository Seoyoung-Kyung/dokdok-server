package com.dokdok.global.util;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.user.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

/**
 * Spring Security의 인증 정보를 쉽게 조회하기 위한 유틸리티 클래스
 */
@Slf4j
public class SecurityUtil {

    private SecurityUtil() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 현재 인증된 사용자의 CustomOAuth2User 객체를 반환
     *
     * @return CustomOAuth2User 객체
     * @throws GlobalException 인증되지 않았거나 유효하지 않은 경우
     */
    public static CustomOAuth2User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            log.warn("인증되지 않은 사용자의 접근 시도");
            throw new GlobalException(GlobalErrorCode.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (!(principal instanceof CustomOAuth2User)) {
            log.warn("유효하지 않은 인증 principal 타입: {}", principal.getClass().getName());
            throw new GlobalException(GlobalErrorCode.UNAUTHORIZED);
        }

        return (CustomOAuth2User) principal;
    }

    /**
     * 현재 인증된 사용자의 ID를 반환
     *
     * @return 사용자 ID
     * @throws GlobalException 인증되지 않았거나 유효하지 않은 경우
     */
    public static Long getCurrentUserId() {
        return getCurrentUser().getUserId();
    }

    /**
     * 현재 인증된 사용자의 User 엔티티를 반환
     *
     * @return User 엔티티
     * @throws GlobalException 인증되지 않았거나 유효하지 않은 경우
     */
    public static User getCurrentUserEntity() {
        return getCurrentUser().getUser();
    }

    /**
     * 현재 인증된 사용자의 닉네임을 반환
     *
     * @return 사용자 닉네임
     * @throws GlobalException 인증되지 않았거나 유효하지 않은 경우
     */
    public static String getCurrentUserNickname() {
        return getCurrentUser().getNickname();
    }

    /**
     * 현재 사용자가 인증되었는지 확인
     *
     * @return 인증 여부
     */
    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof CustomOAuth2User;
    }

    /**
     * 현재 인증된 사용자의 CustomOAuth2User 객체를 Optional로 반환
     * 인증되지 않은 경우 예외를 던지지 않고 Optional.empty() 반환
     *
     * @return Optional<CustomOAuth2User>
     */
    public static Optional<CustomOAuth2User> getCurrentUserOptional() {
        try {
            return Optional.of(getCurrentUser());
        } catch (GlobalException e) {
            return Optional.empty();
        }
    }

    /**
     * 현재 인증된 사용자의 ID를 Optional로 반환
     * 인증되지 않은 경우 예외를 던지지 않고 Optional.empty() 반환
     *
     * @return Optional<Long>
     */
    public static Optional<Long> getCurrentUserIdOptional() {
        return getCurrentUserOptional().map(CustomOAuth2User::getUserId);
    }

    /**
     * SecurityContext의 인증 정보를 업데이트
     * User 엔티티가 업데이트된 경우 SecurityContext도 함께 갱신하기 위해 사용
     *
     * @param updatedUser 업데이트된 User 엔티티
     * @throws GlobalException 현재 인증되지 않은 경우
     */
    public static void updateCurrentUserInContext(User updatedUser) {
        CustomOAuth2User currentOAuth2User = getCurrentUser();

        // 기존 attributes를 유지하면서 업데이트된 User로 새 CustomOAuth2User 생성
        CustomOAuth2User updatedOAuth2User = CustomOAuth2User.builder()
                .user(updatedUser)
                .attributes(currentOAuth2User.getAttributes())
                .build();

        // SecurityContext에 새로운 Authentication 설정
        Authentication newAuth = new UsernamePasswordAuthenticationToken(
                updatedOAuth2User,
                null,
                updatedOAuth2User.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(newAuth);

        log.info("SecurityContext 업데이트 완료: userId={}, nickname={}",
                updatedUser.getId(), updatedUser.getNickname());
    }
}