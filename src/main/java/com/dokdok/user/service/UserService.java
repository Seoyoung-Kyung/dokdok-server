package com.dokdok.user.service;

import com.dokdok.user.entity.User;
import com.dokdok.user.exception.UserErrorCode;
import com.dokdok.user.exception.UserException;
import com.dokdok.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    /**
     * 닉네임 변경 전 중복 체크
     * @param nickname 변경할 닉네임
     */
    public void checkNickname(String nickname) {
        // 1. null 또는 빈 문자열 체크 (trim 전에 먼저 체크)
        if(nickname == null || nickname.trim().isEmpty()) {
            throw new UserException(UserErrorCode.NICKNAME_EMPTY);
        }

        String trimmedNickname = nickname.trim();
        validateNickname(trimmedNickname);

        Optional<User> checkNick = userRepository.findByNickname(trimmedNickname);
        if(checkNick.isPresent()) {
            throw new UserException(UserErrorCode.NICKNAME_ALREADY_EXISTS);
        }
    }

    /**
     * 닉네임 유효성을 검사합니다.
     * @param nickname trim된 닉네임
     */
    private void validateNickname(String nickname) {
        if(nickname.length() < 2 || nickname.length() > 20) {
            throw new UserException(UserErrorCode.NICKNAME_LENGTH_INVALID);
        }

        if(!nickname.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new UserException(UserErrorCode.NICKNAME_FORMAT_INVALID);
        }
    }

}
