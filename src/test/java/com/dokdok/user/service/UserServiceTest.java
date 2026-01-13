package com.dokdok.user.service;

import com.dokdok.global.util.SecurityUtil;
import com.dokdok.user.dto.request.OnboardRequest;
import com.dokdok.user.entity.User;
import com.dokdok.user.exception.UserErrorCode;
import com.dokdok.user.exception.UserException;
import com.dokdok.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService 테스트")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = User.builder()
                .id(1L)
                .nickname("기존닉네임")
                .build();
    }

    @Test
    @DisplayName("정상적인 닉네임 검증 - 성공")
    void checkNickname_Success() {
        // given
        String validNickname = "새로운닉네임";
        when(userRepository.findByNickname(validNickname)).thenReturn(Optional.empty());

        // when & then
        assertDoesNotThrow(() -> userService.checkNickname(validNickname));
        verify(userRepository, times(1)).findByNickname(validNickname);
    }

    @Test
    @DisplayName("영문과 숫자가 포함된 닉네임 검증 - 성공")
    void checkNickname_WithEnglishAndNumber_Success() {
        // given
        String validNickname = "User123";
        when(userRepository.findByNickname(validNickname)).thenReturn(Optional.empty());

        // when & then
        assertDoesNotThrow(() -> userService.checkNickname(validNickname));
        verify(userRepository, times(1)).findByNickname(validNickname);
    }

    @Test
    @DisplayName("앞뒤 공백이 있는 닉네임 - trim 처리 후 성공")
    void checkNickname_WithWhitespace_TrimAndSuccess() {
        // given
        String nicknameWithWhitespace = "  테스트닉네임  ";
        String trimmedNickname = "테스트닉네임";
        when(userRepository.findByNickname(trimmedNickname)).thenReturn(Optional.empty());

        // when & then
        assertDoesNotThrow(() -> userService.checkNickname(nicknameWithWhitespace));
        verify(userRepository, times(1)).findByNickname(trimmedNickname);
    }

    @Test
    @DisplayName("null 닉네임 - NICKNAME_EMPTY 예외 발생")
    void checkNickname_Null_ThrowsException() {
        // given
        String nullNickname = null;

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(nullNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_EMPTY);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("빈 문자열 닉네임 - NICKNAME_EMPTY 예외 발생")
    void checkNickname_EmptyString_ThrowsException() {
        // given
        String emptyNickname = "";

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(emptyNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_EMPTY);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("공백만 있는 닉네임 - NICKNAME_EMPTY 예외 발생")
    void checkNickname_OnlyWhitespace_ThrowsException() {
        // given
        String whitespaceNickname = "   ";

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(whitespaceNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_EMPTY);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("1자 닉네임 - NICKNAME_LENGTH_INVALID 예외 발생")
    void checkNickname_TooShort_ThrowsException() {
        // given
        String shortNickname = "a";

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(shortNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_LENGTH_INVALID);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("21자 닉네임 - NICKNAME_LENGTH_INVALID 예외 발생")
    void checkNickname_TooLong_ThrowsException() {
        // given
        String longNickname = "a".repeat(21);

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(longNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_LENGTH_INVALID);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("2자 닉네임 - 최소 길이 통과")
    void checkNickname_MinLength_Success() {
        // given
        String minLengthNickname = "ab";
        when(userRepository.findByNickname(minLengthNickname)).thenReturn(Optional.empty());

        // when & then
        assertDoesNotThrow(() -> userService.checkNickname(minLengthNickname));
        verify(userRepository, times(1)).findByNickname(minLengthNickname);
    }

    @Test
    @DisplayName("20자 닉네임 - 최대 길이 통과")
    void checkNickname_MaxLength_Success() {
        // given
        String maxLengthNickname = "a".repeat(20);
        when(userRepository.findByNickname(maxLengthNickname)).thenReturn(Optional.empty());

        // when & then
        assertDoesNotThrow(() -> userService.checkNickname(maxLengthNickname));
        verify(userRepository, times(1)).findByNickname(maxLengthNickname);
    }

    @Test
    @DisplayName("특수문자 포함 닉네임 - NICKNAME_FORMAT_INVALID 예외 발생")
    void checkNickname_WithSpecialCharacters_ThrowsException() {
        // given
        String specialCharNickname = "닉네임!@#";

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(specialCharNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_FORMAT_INVALID);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("공백 포함 닉네임 - NICKNAME_FORMAT_INVALID 예외 발생")
    void checkNickname_WithSpace_ThrowsException() {
        // given
        String spaceNickname = "닉네 임";

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(spaceNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_FORMAT_INVALID);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("이모지 포함 닉네임 - NICKNAME_FORMAT_INVALID 예외 발생")
    void checkNickname_WithEmoji_ThrowsException() {
        // given
        String emojiNickname = "닉네임😀";

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(emojiNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_FORMAT_INVALID);

        verify(userRepository, never()).findByNickname(anyString());
    }

    @Test
    @DisplayName("중복된 닉네임 - NICKNAME_ALREADY_EXISTS 예외 발생")
    void checkNickname_Duplicate_ThrowsException() {
        // given
        String duplicateNickname = "기존닉네임";
        when(userRepository.findByNickname(duplicateNickname)).thenReturn(Optional.of(mockUser));

        // when & then
        assertThatThrownBy(() -> userService.checkNickname(duplicateNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_ALREADY_EXISTS);

        verify(userRepository, times(1)).findByNickname(duplicateNickname);
    }

    @Test
    @DisplayName("대소문자가 다른 닉네임 - 대소문자 구분하여 중복 체크")
    void checkNickname_CaseSensitive_Success() {
        // given
        String upperCaseNickname = "NICKNAME";
        String lowerCaseNickname = "nickname";

        when(userRepository.findByNickname(upperCaseNickname)).thenReturn(Optional.empty());
        when(userRepository.findByNickname(lowerCaseNickname)).thenReturn(Optional.of(mockUser));

        // when & then
        assertDoesNotThrow(() -> userService.checkNickname(upperCaseNickname));
        assertThatThrownBy(() -> userService.checkNickname(lowerCaseNickname))
                .isInstanceOf(UserException.class)
                .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_ALREADY_EXISTS);

        verify(userRepository, times(1)).findByNickname(upperCaseNickname);
        verify(userRepository, times(1)).findByNickname(lowerCaseNickname);
    }

    @Test
    @DisplayName("한글+영문+숫자 조합 닉네임 - 성공")
    void checkNickname_MixedLanguage_Success() {
        // given
        String mixedNickname = "유저User123";
        when(userRepository.findByNickname(mixedNickname)).thenReturn(Optional.empty());

        // when & then
        assertDoesNotThrow(() -> userService.checkNickname(mixedNickname));
        verify(userRepository, times(1)).findByNickname(mixedNickname);
    }

    @Test
    @DisplayName("온보딩 - 정상적인 닉네임으로 성공")
    void onboard_Success() {
        // given
        Long userId = 1L;
        String newNickname = "새로운닉네임";
        OnboardRequest request = new OnboardRequest(newNickname);

        User user = User.builder()
                .id(userId)
                .kakaoId(12345L)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByNickname(newNickname)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when
            userService.onboard(request);

            // then
            assertThat(user.getNickname()).isEqualTo(newNickname);
            verify(userRepository, times(1)).findById(userId);
            verify(userRepository, times(1)).findByNickname(newNickname);
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(user), times(1));
        }
    }

    @Test
    @DisplayName("온보딩 - 공백 포함 닉네임 trim 처리 후 성공")
    void onboard_WithWhitespace_TrimAndSuccess() {
        // given
        Long userId = 1L;
        String nicknameWithWhitespace = "  새로운닉네임  ";
        String trimmedNickname = "새로운닉네임";
        OnboardRequest request = new OnboardRequest(nicknameWithWhitespace);

        User user = User.builder()
                .id(userId)
                .kakaoId(12345L)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.findByNickname(trimmedNickname)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when
            userService.onboard(request);

            // then
            assertThat(user.getNickname()).isEqualTo(trimmedNickname);
            verify(userRepository, times(1)).findByNickname(trimmedNickname);
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(user), times(1));
        }
    }

    @Test
    @DisplayName("온보딩 - 중복된 닉네임으로 실패")
    void onboard_DuplicateNickname_ThrowsException() {
        // given
        Long userId = 1L;
        String duplicateNickname = "기존닉네임";
        OnboardRequest request = new OnboardRequest(duplicateNickname);

        when(userRepository.findByNickname(duplicateNickname)).thenReturn(Optional.of(mockUser));

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            assertThatThrownBy(() -> userService.onboard(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_ALREADY_EXISTS);

            verify(userRepository, never()).findById(anyLong());
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(any()), never());
        }
    }

    @Test
    @DisplayName("온보딩 - null 닉네임으로 실패")
    void onboard_NullNickname_ThrowsException() {
        // given
        Long userId = 1L;
        OnboardRequest request = new OnboardRequest(null);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            assertThatThrownBy(() -> userService.onboard(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_EMPTY);

            verify(userRepository, never()).findById(anyLong());
            verify(userRepository, never()).findByNickname(anyString());
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(any()), never());
        }
    }

    @Test
    @DisplayName("온보딩 - 빈 닉네임으로 실패")
    void onboard_EmptyNickname_ThrowsException() {
        // given
        Long userId = 1L;
        String emptyNickname = "";
        OnboardRequest request = new OnboardRequest(emptyNickname);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            assertThatThrownBy(() -> userService.onboard(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_EMPTY);

            verify(userRepository, never()).findById(anyLong());
            verify(userRepository, never()).findByNickname(anyString());
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(any()), never());
        }
    }

    @Test
    @DisplayName("온보딩 - 길이가 짧은 닉네임으로 실패")
    void onboard_TooShortNickname_ThrowsException() {
        // given
        Long userId = 1L;
        String shortNickname = "a";
        OnboardRequest request = new OnboardRequest(shortNickname);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            assertThatThrownBy(() -> userService.onboard(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_LENGTH_INVALID);

            verify(userRepository, never()).findById(anyLong());
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(any()), never());
        }
    }

    @Test
    @DisplayName("온보딩 - 길이가 긴 닉네임으로 실패")
    void onboard_TooLongNickname_ThrowsException() {
        // given
        Long userId = 1L;
        String longNickname = "a".repeat(21);
        OnboardRequest request = new OnboardRequest(longNickname);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            assertThatThrownBy(() -> userService.onboard(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_LENGTH_INVALID);

            verify(userRepository, never()).findById(anyLong());
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(any()), never());
        }
    }

    @Test
    @DisplayName("온보딩 - 특수문자 포함 닉네임으로 실패")
    void onboard_InvalidFormat_ThrowsException() {
        // given
        Long userId = 1L;
        String invalidNickname = "닉네임!@#";
        OnboardRequest request = new OnboardRequest(invalidNickname);

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            assertThatThrownBy(() -> userService.onboard(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.NICKNAME_FORMAT_INVALID);

            verify(userRepository, never()).findById(anyLong());
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(any()), never());
        }
    }

    @Test
    @DisplayName("온보딩 - 존재하지 않는 사용자로 실패")
    void onboard_UserNotFound_ThrowsException() {
        // given
        Long userId = 1L;
        String validNickname = "새로운닉네임";
        OnboardRequest request = new OnboardRequest(validNickname);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        when(userRepository.findByNickname(validNickname)).thenReturn(Optional.empty());

        try (MockedStatic<SecurityUtil> securityUtilMock = mockStatic(SecurityUtil.class)) {
            securityUtilMock.when(SecurityUtil::getCurrentUserId).thenReturn(userId);

            // when & then
            assertThatThrownBy(() -> userService.onboard(request))
                    .isInstanceOf(UserException.class)
                    .hasFieldOrPropertyWithValue("errorCode", UserErrorCode.USER_NOT_FOUND);

            verify(userRepository, times(1)).findById(userId);
            securityUtilMock.verify(() -> SecurityUtil.updateCurrentUserInContext(any()), never());
        }
    }
}