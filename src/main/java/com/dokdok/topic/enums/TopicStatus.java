package com.dokdok.topic.enums;

import lombok.Getter;

@Getter
public enum TopicStatus {
    PROPOSED("제안됨"),
    VOTING("투표 진행중"),
    CONFIRMED("확정됨"),
    REJECTED("거부됨"),
    TEMPLATE_CREATED("템플릿 생성 완료"),
    IN_PROGRESS("답변 작성 중"),     
    COMPLETED("완료"),      
    ARCHIVED("보관됨");

    private final String displayName;

    TopicStatus(String displayName) {
        this.displayName = displayName;
    }

}
