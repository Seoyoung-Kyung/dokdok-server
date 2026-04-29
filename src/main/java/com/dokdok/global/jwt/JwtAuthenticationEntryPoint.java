package com.dokdok.global.jwt;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json;charset=UTF-8");
        GlobalErrorCode errorCode = GlobalErrorCode.UNAUTHORIZED;
        ApiResponse<Void> apiResponse = new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
