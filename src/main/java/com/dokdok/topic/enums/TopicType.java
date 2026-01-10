package com.dokdok.topic.enums;

import lombok.Getter;

@Getter
public enum TopicType {
    FREE("자유형"),
    DISCUSSION("토론형"),
    EMOTION("감정 공유형"),
    EXPERIENCE("경험 연결형"),
    CHARACTER_ANALYSIS("인물 분석형"),
    COMPARISON("비교 분석형"),
    STRUCTURE("구조 분석형"),
    IN_DEPTH("심층 분석형"),
    CREATIVE("창작형"),
    CUSTOM("커스텀");

    private final String displayName;

    TopicType(String displayName) {
        this.displayName = displayName;
    }

}