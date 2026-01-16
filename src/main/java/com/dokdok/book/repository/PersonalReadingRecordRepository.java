package com.dokdok.book.repository;

import com.dokdok.book.entity.PersonalReadingRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PersonalReadingRecordRepository extends JpaRepository<PersonalReadingRecord, Long> {
}
