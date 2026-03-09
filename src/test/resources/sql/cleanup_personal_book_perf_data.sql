-- =============================================================
-- 책장 페이지네이션 성능 테스트 더미 데이터 정리 스크립트
-- =============================================================

-- book_review (FK: book_id, user_id)
DELETE FROM book_review
WHERE user_id = (
    SELECT user_id FROM users
    WHERE user_email = 'book-perf@dokdok.com'
);

-- personal_book
DELETE FROM personal_book
WHERE user_id = (
    SELECT user_id FROM users
    WHERE user_email = 'book-perf@dokdok.com'
);

-- book
DELETE FROM book
WHERE isbn LIKE 'BOOK-PERF-ISBN-%';

-- users
DELETE FROM users
WHERE user_email = 'book-perf@dokdok.com';
