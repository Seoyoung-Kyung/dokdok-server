package com.dokdok.history.repository;

import com.dokdok.history.entity.BookReviewHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BookReviewHistoryRepository extends JpaRepository<BookReviewHistory, Long> {
}
