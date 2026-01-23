package com.dokdok.book.repository;

import com.dokdok.book.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    Optional<BookReview> findByBookIdAndUserId(Long bookId, Long userId);

    boolean existsByBookIdAndUserId(Long bookId, Long userId);

    List<BookReview> findByMeetingId(Long meetingId);

    boolean existsByMeetingIdAndUserId(Long userId);

}