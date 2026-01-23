package com.dokdok.book.repository;

import com.dokdok.book.entity.PersonalReadingRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PersonalReadingRecordRepository extends JpaRepository<PersonalReadingRecord, Long> {
    Optional<PersonalReadingRecord> findByIdAndPersonalBook_Book_IdAndUserId(Long id, Long bookId, Long userId);
    Page<PersonalReadingRecord> findAllByPersonalBook_Book_IdAndUserId(Long bookId, Long userId, Pageable pageable);
}
