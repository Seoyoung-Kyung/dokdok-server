package com.dokdok.book.repository;

import com.dokdok.book.entity.BookReadingStatus;
import com.dokdok.book.entity.PersonalBook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PersonalBookRepository extends JpaRepository<PersonalBook, Long> {
    Optional<PersonalBook> findByUserIdAndBookId(Long userId, Long bookId);
    Page<PersonalBook> findByUserId(Long userId, Pageable pageable);
    Page<PersonalBook> findAllByUserIdAndReadingStatus(Long userId, BookReadingStatus bookReadingStatus, Pageable pageable);
    @Query("""
      SELECT pb FROM PersonalBook pb
      WHERE pb.user.id = :userId
        AND (:readingStatus IS NULL OR pb.readingStatus = :readingStatus)
        AND (:gatheringId IS NULL OR EXISTS (
              SELECT 1 FROM GatheringBook gb
              WHERE gb.book.id = pb.book.id
                AND gb.gathering.id = :gatheringId
        ))
  """)
    Page<PersonalBook> findAllByUserIdAndFilters(
            Long userId,
            @Param("gatheringId") Long gatheringId,
            @Param("readingStatus") BookReadingStatus readingStatus,
            Pageable pageable);

    @Query(
            value = """
                    (SELECT
                        b.book_id AS bookId,
                        b.book_name AS title,
                        b.publisher AS publisher,
                        b.author AS authors,
                        pb.reading_status AS bookReadingStatus,
                        b.book_image_url AS thumbnail,
                        g.gathering_name AS gatheringName,
                        pb.added_at AS addedAt
                    FROM personal_book pb
                             JOIN book b
                                  ON pb.book_id = b.book_id
                             LEFT JOIN gathering_book gb
                                       ON gb.book_id = b.book_id
                                           AND gb.deleted_at IS NULL
                             LEFT JOIN gathering g
                                       ON g.gathering_id = gb.gathering_id
                             LEFT JOIN gathering_member gm
                                       ON gm.gathering_id = g.gathering_id
                                           AND gm.user_id = pb.user_id
                    WHERE pb.deleted_at IS NULL
                      AND pb.user_id = :userId
                      AND (CAST(:readingStatus AS VARCHAR) IS NULL OR pb.reading_status = CAST(:readingStatus AS VARCHAR))
                      AND (:gatheringId IS NULL OR g.gathering_id = :gatheringId))
                    UNION
                    (SELECT
                        b.book_id AS bookId,
                        b.book_name AS title,
                        b.publisher AS publisher,
                        b.author AS authors,
                        NULL AS bookReadingStatus,
                        b.book_image_url AS thumbnail,
                        g.gathering_name AS gatheringName,
                        gb.added_at AS addedAt
                    FROM gathering_book gb
                             JOIN gathering g
                                  ON g.gathering_id = gb.gathering_id
                             JOIN gathering_member gm
                                  ON gm.gathering_id = g.gathering_id
                                      AND gm.user_id = :userId
                             JOIN book b
                                  ON b.book_id = gb.book_id
                             LEFT JOIN personal_book pb
                                       ON pb.book_id = b.book_id
                                           AND pb.user_id = :userId
                                           AND pb.deleted_at IS NULL
                    WHERE pb.personal_book_id IS NULL
                      AND gb.deleted_at IS NULL
                      AND gm.removed_at IS NULL
                      AND (:gatheringId IS NULL OR g.gathering_id = :gatheringId)
                      AND CAST(:readingStatus AS VARCHAR) IS NULL)
                    ORDER BY addedAt DESC
                    """,
            countQuery = """
                    SELECT COUNT(*) FROM (
                        (SELECT
                             b.book_id AS bookId
                         FROM personal_book pb
                                  JOIN book b
                                       ON pb.book_id = b.book_id
                                  LEFT JOIN gathering_book gb
                                            ON gb.book_id = b.book_id
                                                AND gb.deleted_at IS NULL
                                  LEFT JOIN gathering g
                                            ON g.gathering_id = gb.gathering_id
                                  LEFT JOIN gathering_member gm
                                            ON gm.gathering_id = g.gathering_id
                                                AND gm.user_id = pb.user_id
                         WHERE pb.deleted_at IS NULL
                           AND pb.user_id = :userId
                           AND (CAST(:readingStatus AS VARCHAR) IS NULL OR pb.reading_status = CAST(:readingStatus AS VARCHAR))
                           AND (:gatheringId IS NULL OR g.gathering_id = :gatheringId))
                        UNION
                        (SELECT
                             b.book_id AS bookId
                         FROM gathering_book gb
                                  JOIN gathering g
                                       ON g.gathering_id = gb.gathering_id
                                  JOIN gathering_member gm
                                       ON gm.gathering_id = g.gathering_id
                                           AND gm.user_id = :userId
                                  JOIN book b
                                       ON b.book_id = gb.book_id
                                  LEFT JOIN personal_book pb
                                            ON pb.book_id = b.book_id
                                                AND pb.user_id = :userId
                                                AND pb.deleted_at IS NULL
                         WHERE pb.personal_book_id IS NULL
                           AND gb.deleted_at IS NULL
                           AND gm.removed_at IS NULL
                           AND (:gatheringId IS NULL OR g.gathering_id = :gatheringId)
                           AND CAST(:readingStatus AS VARCHAR) IS NULL)
                    ) AS total
                    """,
            nativeQuery = true
    )
    Page<PersonalBookListProjection> findMyBooksWithGathering(
            @Param("userId") Long userId,
            @Param("gatheringId") Long gatheringId,
            @Param("readingStatus") BookReadingStatus readingStatus,
            Pageable pageable
    );
}
