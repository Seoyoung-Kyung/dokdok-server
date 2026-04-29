package com.dokdok.global.jwt;

import com.dokdok.global.exception.GlobalErrorCode;
import com.dokdok.global.redis.RefreshTokenRepository;
import com.dokdok.global.response.ApiResponse;
import com.dokdok.oauth2.CustomOAuth2User;
import com.dokdok.user.entity.User;
import com.dokdok.user.repository.UserRepository;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/auth/token");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);

        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            if (refreshTokenRepository.isBlacklisted(token)) {
                log.warn("블랙리스트에 등록된 토큰 접근 시도");
                filterChain.doFilter(request, response);
                return;
            }

            Long userId = jwtProvider.extractUserId(token);
            User user = userRepository.findById(userId).orElse(null);
            if (user == null) {
                filterChain.doFilter(request, response);
                return;
            }

            CustomOAuth2User oAuth2User = CustomOAuth2User.builder()
                    .user(user)
                    .attributes(Map.of())
                    .build();

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(oAuth2User, null, oAuth2User.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } catch (ExpiredJwtException e) {
            sendErrorResponse(response, GlobalErrorCode.EXPIRED_TOKEN);
            return;
        } catch (JwtException e) {
            sendErrorResponse(response, GlobalErrorCode.INVALID_TOKEN);
            return;
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }

    private void sendErrorResponse(HttpServletResponse response, GlobalErrorCode errorCode) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        ApiResponse<Void> apiResponse = new ApiResponse<>(errorCode.getCode(), errorCode.getMessage(), null);
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
