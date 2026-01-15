package com.dokdok.book.service;

import com.dokdok.book.dto.request.PersonalReadingRecordCreateRequest;
import com.dokdok.book.dto.response.PersonalReadingRecordCreateResponse;
import com.dokdok.book.entity.PersonalBook;
import com.dokdok.book.entity.PersonalReadingRecord;
import com.dokdok.book.repository.PersonalReadingRecordRepository;
import com.dokdok.global.util.SecurityUtil;
import com.dokdok.user.entity.User;
import com.dokdok.user.service.UserValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PersonalReadingRecordService {

    private final PersonalReadingRecordRepository personalReadingRecordRepository;
    private final UserValidator userValidator;
    private final BookValidator bookValidator;

    public PersonalReadingRecordCreateResponse create(Long personalBookId, PersonalReadingRecordCreateRequest personalReadingRecordRequest) {

        User userEntity = userValidator.findUserOrThrow(SecurityUtil.getCurrentUserId());
        PersonalBook personalBookEntity = bookValidator.validateInBookShelf(userEntity.getId(),personalBookId);

        PersonalReadingRecord personalReadingRecordEntity = PersonalReadingRecord.create(personalBookEntity, userEntity, personalReadingRecordRequest.recordContent());
        personalReadingRecordRepository.save(personalReadingRecordEntity);

        return PersonalReadingRecordCreateResponse.from(personalReadingRecordEntity);
    }
}
