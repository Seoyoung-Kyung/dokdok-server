package com.dokdok.topic.entity;

import lombok.Getter;

@Getter
public enum TopicType {

    FREE(
            "자유형",
            "자유롭게 이야기 나누는 주제입니다."
    ),
    DISCUSSION(
            "토론형",
            "찬반 토론이나 다양한 관점을 나누는 주제입니다."
    ),
    EMOTION(
            "감정 공유형",
            "책을 읽으며 느낀 감정을 공유하는 주제입니다."
    ),
    EXPERIENCE(
            "경험 연결형",
            "책의 내용을 개인 경험과 연결하는 주제입니다."
    ),
    CHARACTER_ANALYSIS(
            "인물 분석형",
            "등장인물의 성격, 동기, 변화를 분석하는 주제입니다."
    ),
    COMPARISON(
            "비교 분석형",
            "다른 작품이나 현실과 비교하는 주제입니다."
    ),
    STRUCTURE(
            "구조 분석형",
            "책의 구성, 서술 방식, 문체를 분석하는 주제입니다."
    ),
    IN_DEPTH(
            "심층 분석형",
            "주제, 상징, 메시지를 깊이 있게 분석하는 주제입니다."
    ),
    CREATIVE(
            "창작형",
            "후속 이야기나 다른 결말을 창작하는 주제입니다."
    ),
    CUSTOM(
            "질문형",
            "궁금한 점을 질문하고 함께 답을 찾는 주제입니다."
    );

    private final String displayName;
    private final String description;

    TopicType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
}
