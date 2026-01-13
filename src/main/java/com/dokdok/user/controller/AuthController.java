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
import org.springframework.web.bind.annotation.RestController;


@Slf4j
@RestController
public class AuthController implements AuthApi {

    @Override
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
}
