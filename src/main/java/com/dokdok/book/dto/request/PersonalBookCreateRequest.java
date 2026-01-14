package com.dokdok.book.dto.request;

import lombok.Builder;

@Builder
public record PersonalBookCreateRequest(
        String isbn
) {
}
