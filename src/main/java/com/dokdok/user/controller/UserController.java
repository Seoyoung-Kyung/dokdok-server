package com.dokdok.user.controller;

import com.dokdok.global.response.ApiResponse;
import com.dokdok.user.api.UserApi;
import com.dokdok.user.dto.request.OnboardRequestDto;
import com.dokdok.user.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController implements UserApi {

    private final UserService userService;

    @PatchMapping("/onboarding")
    public ResponseEntity<ApiResponse<Void>> onboard(OnboardRequestDto request) {
        userService.onboard(request);
        return ApiResponse.success("온보딩 완료");
    }

    @GetMapping(value = "/check-nickname")
    @Override
    public ResponseEntity<ApiResponse<Void>> checkNickname(String nickname) {
        userService.checkNickname(nickname);
        return ApiResponse.success("사용 가능한 닉네임입니다.");
    }
}
