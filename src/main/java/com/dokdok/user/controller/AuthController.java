package com.dokdok.user.controller;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.exception.GlobalException;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.user.api.AuthApi;
import com.dokdok.user.dto.response.UserInfoResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
@RequestMapping("/api/auth")
public class AuthController implements AuthApi {

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
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout() {
        // 실제 처리는 Security LogoutFilter가 수행하며, 명세/시그니처 충족용
        return ApiResponse.success("로그아웃 성공");
    }
}
