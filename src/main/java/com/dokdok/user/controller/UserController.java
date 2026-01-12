package com.dokdok.user.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.user.api.UserApi;
import com.dokdok.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @Override
    public ResponseEntity<ApiResponse<Void>> checkNickname(String nickname) {
        userService.checkNickname(nickname);
        return ApiResponse.success("사용 가능한 닉네임입니다.");
    }
}
