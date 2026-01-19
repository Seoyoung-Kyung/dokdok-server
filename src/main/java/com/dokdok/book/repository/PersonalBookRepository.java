package com.dokdok.book.repository;

import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalBookRepository extends JpaRepository<PersonalBook, Long> {
    Optional<PersonalBook> findByUserIdAndId(Long userId, Long Id);
    Optional<PersonalBook> findByUserIdAndBookId(Long userId, Long bookId);
    Page<PersonalBook> findByUserId(Long userId, Pageable pageable);
    Page<PersonalBook> findAllByUserIdAndReadingStatus(Long userId, BookReadingStatus bookReadingStatus, Pageable pageable);
}
