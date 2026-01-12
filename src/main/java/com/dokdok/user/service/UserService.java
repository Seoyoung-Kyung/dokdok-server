package com.dokdok.user.service;

import com.dokdok.global.util.SecurityUtil;
import com.dokdok.user.dto.request.OnboardRequestDto;
import com.dokdok.user.entity.User;
import com.dokdok.user.exception.UserErrorCode;
import com.dokdok.user.exception.UserException;
import com.dokdok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public void onboard(OnboardRequestDto request) {

        Long currentUserId = SecurityUtil.getCurrentUserId();

        if (request.nickname() == null) {
            throw new UserException(UserErrorCode.NICKNAME_EMPTY);
        }

        String trimmedNickname = request.nickname().trim();
        validateNickname(trimmedNickname);

        User user = getUserById(currentUserId);
        user.updateNickname(trimmedNickname);

        SecurityUtil.updateCurrentUserInContext(user);
    }

    /**
     * 닉네임 변경 전 중복 체크
     * @param nickname 변경할 닉네임
     */
    @Transactional(readOnly = true)
    public void checkNickname(String nickname) {
        if (nickname == null) {
            throw new UserException(UserErrorCode.NICKNAME_EMPTY);
        }
        validateNickname(nickname.trim());
    }


    /**
     * 닉네임 유효성을 검사합니다.
     * @param nickname trim된 닉네임
     */
    private void validateNickname(String nickname) {
        if(nickname == null || nickname.trim().isEmpty()) {
            throw new UserException(UserErrorCode.NICKNAME_EMPTY);
        }

        if(nickname.length() < 2 || nickname.length() > 20) {
            throw new UserException(UserErrorCode.NICKNAME_LENGTH_INVALID);
        }

        if(!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new UserException(UserErrorCode.NICKNAME_FORMAT_INVALID);
        }

        Optional<User> checkNick = userRepository.findByNickname(nickname);
        if(checkNick.isPresent()) {
            throw new UserException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    private User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new UserException(UserErrorCode.USER_NOT_FOUND));
    }
}
