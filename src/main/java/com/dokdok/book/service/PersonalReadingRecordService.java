package com.dokdok.book.service;

import com.dokdok.book.dto.request.PersonalReadingRecordCreateRequest;
import com.dokdok.book.dto.response.PersonalReadingRecordCreateResponse;
import com.dokdok.book.dto.response.PersonalReadingRecordListResponse;
import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.entity.PersonalReadingRecord;
import com.dokdok.book.entity.RecordType;
import com.dokdok.book.exception.RecordErrorCode;
import com.dokdok.book.exception.RecordException;
import com.dokdok.book.dto.request.PersonalReadingRecordUpdateRequest;
import com.dokdok.book.repository.PersonalReadingRecordRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.user.entity.User;
import com.dokdok.user.service.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PersonalReadingRecordService {

    private final PersonalReadingRecordRepository personalReadingRecordRepository;
    private final UserValidator userValidator;
    private final BookValidator bookValidator;

    @Transactional
    public PersonalReadingRecordCreateResponse create(Long personalBookId, PersonalReadingRecordCreateRequest request) {
        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        PersonalBook personalBookEntity = bookValidator.validateInBookShelf(userEntity.getId(), personalBookId);

        Map<String, Object> normalizedMeta = normalizeMeta(request.recordType(), request.meta());

        PersonalReadingRecord personalReadingRecordEntity =
                PersonalReadingRecord.create(
                        personalBookEntity,
                        userEntity,
                        request.recordType(),
                        request.recordContent(),
                        normalizedMeta
                        );
        personalReadingRecordRepository.save(personalReadingRecordEntity);

        return PersonalReadingRecordCreateResponse.from(personalReadingRecordEntity);
    }

    @Transactional
    public PersonalReadingRecordCreateResponse update(Long personalBookId, Long recordId, PersonalReadingRecordUpdateRequest request) {
        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        PersonalBook personalBookEntity = bookValidator.validateInBookShelf(userEntity.getId(), personalBookId);

        PersonalReadingRecord personalReadingRecord = personalReadingRecordRepository
                .findByIdAndPersonalBookIdAndUserId(recordId, personalBookEntity.getId(), userEntity.getId())
                .orElseThrow(() -> new RecordException(RecordErrorCode.RECORD_NOT_FOUND));

        Map<String, Object> normalizedMeta = normalizeMeta(request.recordType(), request.meta());

        personalReadingRecord.update(
                request.recordType(),
                request.recordContent(),
                normalizedMeta
        );

        return PersonalReadingRecordCreateResponse.from(personalReadingRecord);
    }

    @Transactional
    public void delete(Long personalBookId, Long recordId) {
        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        PersonalBook personalBookEntity = bookValidator.validateInBookShelf(userEntity.getId(), personalBookId);

        PersonalReadingRecord personalReadingRecord = personalReadingRecordRepository
                .findByIdAndPersonalBookIdAndUserId(recordId, personalBookEntity.getId(), userEntity.getId())
                .orElseThrow(() -> new RecordException(RecordErrorCode.RECORD_NOT_FOUND));

        if (personalReadingRecord.isDeleted()) {
            throw new RecordException(RecordErrorCode.RECORD_ALREADY_DELETED);
        }

        personalReadingRecord.delete();
    }

    public Page<PersonalReadingRecordListResponse> getRecords(Long personalBookId, Pageable pageable) {

        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        PersonalBook personalBookEntity = bookValidator.validateInBookShelf(userEntity.getId(), personalBookId);

        Page<PersonalReadingRecord> entities = personalReadingRecordRepository.findAllByPersonalBookIdAndUserId(
                personalBookEntity.getId(),
                userEntity.getId(),
                pageable
        );

        return entities.map(PersonalReadingRecordListResponse::from);
    }

    private Map<String, Object> normalizeMeta(RecordType recordType, Map<String, Object> meta) {
        if (recordType == null) {
            throw new RecordException(RecordErrorCode.INVALID_RECORD_TYPE);
        }

        if (recordType == RecordType.MEMO) {
            return null;
        }
        if (recordType == RecordType.QUOTE) {
            if (meta == null) {
                throw new RecordException(RecordErrorCode.INVALID_RECORD_REQUEST);
            }

            Object page = meta.get("page");
            Object excerpt = meta.get("excerpt");

            if (page == null || excerpt == null) {
                throw new RecordException(RecordErrorCode.INVALID_RECORD_REQUEST);
            }
            meta.put("page", Integer.parseInt(String.valueOf(page)));
            meta.put("excerpt", String.valueOf(excerpt));
        }
        return meta;
    }

}
