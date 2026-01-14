package com.dokdok.topic.entity;

import lombok.Getter;

@Getter
public enum TopicMessage {
    LIKE_SUCCESS("주제를 좋아요 했습니다."),
    LIKE_CANCEL("주제 좋아요를 취소했습니다.");

    private final String message;

    TopicMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public static TopicMessage from(boolean liked) {
        return liked ? LIKE_SUCCESS : LIKE_CANCEL;
    }
}
