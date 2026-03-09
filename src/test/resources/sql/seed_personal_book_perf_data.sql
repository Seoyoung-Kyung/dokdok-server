-- =============================================================
-- 책장 페이지네이션 성능 테스트용 더미 데이터 삽입 스크립트
--
-- 생성 데이터:
--   테스트 유저 (book-perf@dokdok.com)
--     Book               : 200권 (BOOK-PERF-ISBN-{i})
--     PersonalBook       : 200개 (added_at 1시간 간격)
--     BookReview         : 200건 (rating 1.0~5.0 분포)
-- =============================================================

-- ──────────────────────────────────────────────────────────────
-- [A] 테스트 유저
-- ──────────────────────────────────────────────────────────────
INSERT INTO users (nickname, user_email, kakao_id, created_at, updated_at)
VALUES ('책장성능테스트유저', 'book-perf@dokdok.com', 9200000001, NOW(), NOW());

-- ──────────────────────────────────────────────────────────────
-- [B] Book 200권
-- ──────────────────────────────────────────────────────────────
INSERT INTO book (book_name, author, publisher, isbn, thumbnail, created_at, updated_at)
SELECT
    '성능테스트책 ' || i,
    '저자' || i,
    '테스트출판사',
    'BOOK-PERF-ISBN-' || i,
    'https://example.com/book-perf-' || i || '.jpg',
    NOW(),
    NOW()
FROM generate_series(1, 200) AS i;

-- ──────────────────────────────────────────────────────────────
-- [C] PersonalBook 200개 (added_at: 200시간 전 ~ 1시간 전, 1시간 간격)
-- ──────────────────────────────────────────────────────────────
INSERT INTO personal_book (user_id, book_id, gathering_id, reading_status, added_at, updated_at)
SELECT
    u.user_id,
    b.book_id,
    NULL,
    'READING',
    NOW() - (i * INTERVAL '1 hour'),
    NOW()
FROM generate_series(1, 200) AS i
CROSS JOIN users u
JOIN book b ON b.isbn = 'BOOK-PERF-ISBN-' || i
WHERE u.user_email = 'book-perf@dokdok.com' AND u.deleted_at IS NULL;

-- ──────────────────────────────────────────────────────────────
-- [D] BookReview 200건 (rating: 1.0~5.0 순환 분포)
-- ──────────────────────────────────────────────────────────────
INSERT INTO book_review (book_id, user_id, rating, created_at, updated_at)
SELECT
    b.book_id,
    u.user_id,
    (1 + (i - 1) % 5)::numeric(2, 1),
    NOW(),
    NOW()
FROM generate_series(1, 200) AS i
CROSS JOIN users u
JOIN book b ON b.isbn = 'BOOK-PERF-ISBN-' || i
WHERE u.user_email = 'book-perf@dokdok.com' AND u.deleted_at IS NULL;
