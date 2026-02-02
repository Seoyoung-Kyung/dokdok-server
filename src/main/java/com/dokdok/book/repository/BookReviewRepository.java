package com.dokdok.book.repository;

import com.dokdok.book.entity.BookReview;
import com.dokdok.gathering.dto.response.BookRatingAverage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface BookReviewRepository extends JpaRepository<BookReview, Long> {
    Optional<BookReview> findByBookIdAndUserId(Long bookId, Long userId);

    boolean existsByBookIdAndUserId(Long bookId, Long userId);

    @Query("""
            SELECT br
            FROM BookReview br
            WHERE br.user.id = :userId
            AND br.createdAt = (
                    SELECT MAX(br2.createdAt)
                    FROM BookReview br2
                    WHERE br2.user.id = br.user.id
                )
            """)
    List<BookReview> findByUserId(Long userId);

    @Query("""
            SELECT br
            FROM BookReview br
            WHERE br.user.id IN :userIds
            AND br.createdAt = (
                    SELECT MAX(br2.createdAt)
                    FROM BookReview br2
                    WHERE br2.user.id = br.user.id
                )
            """)
    List<BookReview> findByUserIdIn(List<Long> userIds);

    @Query("""
        SELECT new com.dokdok.gathering.dto.response.BookRatingAverage(
                b.id,
                AVG(br.rating)
            )
        FROM BookReview br
        JOIN br.book b
        WHERE br.user.id IN :meetingMemberIds
        AND b.id IN :bookIds
        GROUP BY br.book
    """)
    List<BookRatingAverage> findMeetingBookReviews(List<Long> bookIds, List<Long> meetingMemberIds);

}