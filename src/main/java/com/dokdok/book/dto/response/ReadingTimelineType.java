package com.dokdok.book.dto.response;

import lombok.Getter;

@Getter
public enum ReadingTimelineType {
    PRE_OPINION(0),
    MEETING_RETROSPECTIVE(1),
    PERSONAL_RETROSPECTIVE(2),
    READING_RECORD(3);

    private final int order;

    ReadingTimelineType(int order) {
        this.order = order;
    }

    public static ReadingTimelineType from(String value) {
        return ReadingTimelineType.valueOf(value);
    }
}
