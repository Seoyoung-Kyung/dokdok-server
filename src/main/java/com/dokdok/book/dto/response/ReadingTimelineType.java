package com.dokdok.book.dto.response;

import lombok.Getter;

@Getter
public enum ReadingTimelineType {
    READING_RECORD(4),
    PERSONAL_RETROSPECTIVE(3),
    GROUP_RETROSPECTIVE(2),
    PRE_OPINION(1);

    private final int order;

    ReadingTimelineType(int order) {
        this.order = order;
    }

    public int getOrder() {
        return order;
    }

    public static ReadingTimelineType from(String value) {
        return ReadingTimelineType.valueOf(value);
    }
}
