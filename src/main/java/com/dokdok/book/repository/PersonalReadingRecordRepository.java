package com.dokdok.book.repository;

import com.dokdok.book.entity.PersonalReadingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalReadingRecordRepository extends JpaRepository<PersonalReadingRecord, Long> {
    Optional<PersonalReadingRecord> findByIdAndPersonalBookIdAndUserId(Long id, Long personalBookId, Long userId);
    Page<PersonalReadingRecord> findAllByPersonalBookIdAndUserId(Long personalBookId, Long userId, Pageable pageable);
}
