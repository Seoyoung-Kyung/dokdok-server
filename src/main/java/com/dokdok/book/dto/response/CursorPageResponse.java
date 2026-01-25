package com.dokdok.book.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.List;

@Getter
@AllArgsConstructor
public class CursorPageResponse<T, C> {

    private List<T> items;
    private int pageSize;
    private boolean hasNext;
    private C nextCursor;

    public static <T, C> CursorPageResponse<T, C> of(List<T> items, int pageSize, boolean hasNext, C nextCursor) {
        return new CursorPageResponse<>(items, pageSize, hasNext, nextCursor);
    }
}
