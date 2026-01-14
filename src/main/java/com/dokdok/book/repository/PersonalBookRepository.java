package com.dokdok.book.repository;

import com.dokdok.book.entity.PersonalBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalBookRepository extends JpaRepository<PersonalBook, Long> {
    Optional<PersonalBook> findByUserIdAndBookId(Long userId, Long bookId);
}
