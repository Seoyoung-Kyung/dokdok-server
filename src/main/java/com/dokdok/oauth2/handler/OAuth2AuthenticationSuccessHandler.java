package com.dokdok.oauth2.handler;

import com.dokdok.global.config.FrontendProperties;
import com.dokdok.global.jwt.JwtProvider;
import com.dokdok.global.redis.RefreshTokenRepository;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.oauth2.exception.OAuth2ErrorCode;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Component
@RequiredArgsConstructor
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final FrontendProperties frontendProperties;
    private final ObjectMapper objectMapper;
    private final JwtProvider jwtProvider;
    private final RefreshTokenRepository refreshTokenRepository;

    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 14 * 24 * 60 * 60; // 14일

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        CustomOAuth2User oAuth2User = extractOAuth2User(authentication);

        if (oAuth2User == null) {
            log.error("인증 정보를 추출할 수 없습니다.");
            sendErrorResponse(response,
                    OAuth2ErrorCode.INVALID_USER_PRINCIPAL.getCode(),
                    OAuth2ErrorCode.INVALID_USER_PRINCIPAL.getMessage());
            return;
        }

        Long userId = oAuth2User.getUserId();
        String accessToken = jwtProvider.generateAccessToken(userId);
        String refreshToken = jwtProvider.generateRefreshToken(userId);

        refreshTokenRepository.save(userId, refreshToken);
        setRefreshTokenCookie(response, refreshToken);

        String frontendUrl = getFrontendUrlFromState(request);
        String redirectPath = determineRedirectPath(oAuth2User);
        String redirectUrl = frontendUrl + redirectPath;

        log.info("로그인 성공 리다이렉트: userId={}, url={}", userId, redirectUrl);

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }

    private CustomOAuth2User extractOAuth2User(Authentication authentication) {
        if (authentication == null || authentication.getPrincipal() == null) {
            return null;
        }
        Object principal = authentication.getPrincipal();
        if (principal instanceof CustomOAuth2User customOAuth2User) {
            return customOAuth2User;
        }
        log.error("Principal이 CustomOAuth2User 타입이 아닙니다: {}", principal.getClass().getName());
        return null;
    }

    /**
     * state 파라미터에서 fe_origin 디코딩.
     * state 형식: {originalState}|{base64(feOrigin)}
     */
    private String getFrontendUrlFromState(HttpServletRequest request) {
        String state = request.getParameter("state");
        if (state != null && state.contains("|")) {
            String encodedFeOrigin = state.substring(state.lastIndexOf("|") + 1);
            try {
                String feOrigin = new String(
                        Base64.getUrlDecoder().decode(encodedFeOrigin), StandardCharsets.UTF_8);
                if (!feOrigin.startsWith("http://") && !feOrigin.startsWith("https://")) {
                    feOrigin = "http://" + feOrigin;
                }
                if (frontendProperties.getAllowedOrigins() != null
                        && frontendProperties.getAllowedOrigins().contains(feOrigin)) {
                    log.info("state에서 가져온 FE Origin 사용: {}", feOrigin);
                    return feOrigin;
                }
            } catch (Exception e) {
                log.warn("FE Origin 디코딩 실패: {}", e.getMessage());
            }
        }
        log.info("기본 FE URL 사용: {}", frontendProperties.getDefaultUrl());
        return frontendProperties.getDefaultUrl();
    }

    private String determineRedirectPath(CustomOAuth2User oAuth2User) {
        if (oAuth2User.getUser().getNickname() == null || oAuth2User.getUser().getNickname().isBlank()) {
            log.info("신규 사용자 - 온보딩 필요: userId={}", oAuth2User.getUserId());
            return "/onboarding";
        }
        log.info("기존 사용자 - 홈으로 이동: userId={}, nickname={}",
                oAuth2User.getUserId(), oAuth2User.getNickname());
        return "/home";
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    private void sendErrorResponse(HttpServletResponse response, String code, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = new ApiResponse<>(code, message, null);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
