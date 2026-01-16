package com.dokdok.book.dto.request;

import com.dokdok.book.entity.RecordType;

import java.util.Map;

public record PersonalReadingRecordCreateRequest(
        RecordType recordType,
        String recordContent,
        Map<String, Object> meta
) {

}


