package com.dokdok.user.controller;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.user.api.AuthApi;
import com.dokdok.user.dto.response.AccessTokenResponse;
import com.dokdok.user.dto.response.AuthTokens;
import com.dokdok.user.dto.response.UserInfoResponse;
import com.dokdok.user.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;


@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController implements AuthApi {

    private final AuthService authService;

    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 14 * 24 * 60 * 60; // 14일

    @Override
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getCurrentUser() {
        if (!SecurityUtil.isAuthenticated()) {
            throw new GlobalException(GlobalErrorCode.UNAUTHORIZED);
        }

        CustomOAuth2User currentUser = SecurityUtil.getCurrentUser();
        log.info("인증된 사용자 정보 조회: userId={}, nickname={}",
                currentUser.getUserId(), currentUser.getNickname());

        UserInfoResponse userInfo = UserInfoResponse.from(currentUser.getUser());
        return ApiResponse.success(userInfo, "로그인 사용자 정보 조회 성공");
    }

    @Override
    @GetMapping("/token")
    public ResponseEntity<ApiResponse<AccessTokenResponse>> refreshToken(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {

        if (!StringUtils.hasText(refreshToken)) {
            throw new GlobalException(GlobalErrorCode.REFRESH_TOKEN_NOT_FOUND);
        }

        AuthTokens tokens = authService.issueToken(refreshToken);

        Cookie cookie = new Cookie("refreshToken", tokens.refreshToken());
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(cookie);

        return ApiResponse.success(new AccessTokenResponse(tokens.accessToken()), "액세스 토큰 발급 성공");
    }

    @Override
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            HttpServletResponse response) {

        CustomOAuth2User currentUser = SecurityUtil.getCurrentUser();

        String accessToken = null;
        if (StringUtils.hasText(authorization) && authorization.startsWith("Bearer ")) {
            accessToken = authorization.substring(7);
        }

        authService.logout(accessToken, currentUser.getUserId());

        Cookie cookie = new Cookie("refreshToken", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);

        return ApiResponse.success("로그아웃 성공");
    }
}
