package com.dokdok.book.repository;

import com.dokdok.book.entity.BookReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
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

}