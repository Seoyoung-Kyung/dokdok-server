-- =============================================================
-- 타임라인 성능 테스트용 더미 데이터 삽입 스크립트 (순수 SQL)
--
-- 생성 데이터:
--   테스트 유저 (perf@dokdok.com) — 현실적 규모
--     Book               : 2권 (Book_A: 모임용, Book_B: QUOTE 전용)
--     PersonalBook       : 2개
--     PersonalReadingRecord : 50건 (MEMO 30건/Book_A, QUOTE 20건/Book_B)
--     Gathering          : 1개 (PERF-INVITE-TEST)
--     Meeting            : 20개 (CONFIRMED, 월 1~2회 × 약 1년)
--     MeetingMember      : 20건
--     Topic              : 60개 (미팅당 3개, CONFIRMED)
--     TopicAnswer        : 60건 (is_submitted=TRUE)
--     TopicRetrospectiveSummary : 60건
--     PersonalMeetingRetrospective : 20건
--     RetrospectiveChangedThought  : 60건 (회고당 3건)
--     RetrospectiveFreeText        : 20건 (회고당 1건)
--     RetrospectiveOthersPerspective: 20건 (회고당 1건, 첫 번째 토픽)
--     PersonalReadingRecord : 50건 (MEMO 30건/Book_A, QUOTE 20건/Book_B)
--
--   배경 유저 200명 — PERF-INVITE-TEST 모임에 볼륨 추가
--     (별도 모임 없음 — 모두 PERF-INVITE-TEST + Book_A 사용)
--     각 유저: 미팅 4개, 토픽 3개/미팅, topic_retrospective_summary
--     목표: MEETING_RETROSPECTIVE 쿼리 처리 미팅 수 20 → 820
--
-- 총 규모:
--   users                     : 201
--   gathering                 : 1 (PERF-INVITE-TEST만)
--   meeting                   : 820  (20 + 200×4)
--   topic                     : 2,460 (60 + 200×4×3)
--   topic_retrospective_summary: 2,460 (60 + 200×4×3)
-- =============================================================

-- ─────────────────────────────────────────────────────────────
-- 0. 임시 뷰: 방금 삽입된 행의 ID를 변수처럼 사용하기 위한 CTE 체인
--    (순수 SQL — PL/pgSQL 없음)
-- ─────────────────────────────────────────────────────────────

-- ══════════════════════════════════════════════════════════════
-- [A] 테스트 유저 셋업
-- ══════════════════════════════════════════════════════════════

-- A-1. 테스트 유저
INSERT INTO users (nickname, user_email, kakao_id, created_at, updated_at)
VALUES ('성능테스트유저', 'perf@dokdok.com', 9000000001, NOW(), NOW());

-- A-2. 책 두 권
INSERT INTO book (book_name, author, publisher, isbn, thumbnail, created_at, updated_at)
VALUES
    ('성능 테스트용 책 A', '테스트 저자', '테스트 출판사', 'PERF-ISBN-A', 'https://example.com/book_a.jpg', NOW(), NOW()),
    ('성능 테스트용 책 B', '테스트 저자', '테스트 출판사', 'PERF-ISBN-B', 'https://example.com/book_b.jpg', NOW(), NOW());

-- A-3. 모임
INSERT INTO gathering (gathering_leader_id, gathering_name, description, invitation_link, gathering_status, created_at, updated_at)
SELECT user_id, '성능테스트모임', '성능 테스트를 위한 모임', 'PERF-INVITE-TEST', 'ACTIVE', NOW(), NOW()
FROM users
WHERE user_email = 'perf@dokdok.com' AND deleted_at IS NULL;

-- A-4. personal_book 2개
--   personal_book_A: Book_A + gathering (타임라인 테스트 대상)
INSERT INTO personal_book (user_id, book_id, gathering_id, reading_status, added_at, updated_at)
SELECT u.user_id, b.book_id, g.gathering_id, 'READING', NOW(), NOW()
FROM users u
JOIN book b ON b.isbn = 'PERF-ISBN-A'
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

--   personal_book_B: Book_B, 모임 없음 (QUOTE 기록 전용)
INSERT INTO personal_book (user_id, book_id, gathering_id, reading_status, added_at, updated_at)
SELECT u.user_id, b.book_id, NULL, 'READING', NOW(), NOW()
FROM users u
JOIN book b ON b.isbn = 'PERF-ISBN-B'
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-5. 독서 기록 20건
--   MEMO 10건 → personal_book_A (Book_A)
INSERT INTO personal_reading_record (personal_book_id, user_id, record_type, record_content, is_published, created_at, updated_at)
SELECT pb.personal_book_id, u.user_id,
       'MEMO',
       '메모 기록 ' || i,
       FALSE,
       NOW() - (i * INTERVAL '1 hour'),
       NOW() - (i * INTERVAL '1 hour')
FROM users u
JOIN book b_a ON b_a.isbn = 'PERF-ISBN-A'
JOIN personal_book pb ON pb.user_id = u.user_id AND pb.book_id = b_a.book_id AND pb.deleted_at IS NULL
CROSS JOIN generate_series(1, 30) AS i
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

--   QUOTE 20건 → personal_book_B (Book_B)
INSERT INTO personal_reading_record (personal_book_id, user_id, record_type, record_content, is_published, created_at, updated_at)
SELECT pb.personal_book_id, u.user_id,
       'QUOTE',
       '발췌 기록 ' || i,
       FALSE,
       NOW() - (i * INTERVAL '2 hour'),
       NOW() - (i * INTERVAL '2 hour')
FROM users u
JOIN book b_b ON b_b.isbn = 'PERF-ISBN-B'
JOIN personal_book pb ON pb.user_id = u.user_id AND pb.book_id = b_b.book_id AND pb.deleted_at IS NULL
CROSS JOIN generate_series(1, 20) AS i
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-6. Meeting 5개 (CONFIRMED, Book_A 기준)
INSERT INTO meeting (gathering_id, book_id, meeting_leader_id, meeting_name, meeting_status,
                     meeting_start_date, meeting_end_date, created_at, updated_at)
SELECT g.gathering_id, b.book_id, u.user_id,
       '성능테스트약속 ' || i, 'CONFIRMED',
       NOW() - (i * INTERVAL '7 days'),
       NOW() - (i * INTERVAL '7 days') + INTERVAL '2 hours',
       NOW() - (i * INTERVAL '7 days'),
       NOW() - (i * INTERVAL '7 days')
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN book b ON b.isbn = 'PERF-ISBN-A'
CROSS JOIN generate_series(1, 20) AS i
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-7. MeetingMember 5건
INSERT INTO meeting_member (meeting_id, user_id, meeting_role, attendance_status, joined_at, updated_at)
SELECT m.meeting_id, u.user_id, 'MEMBER', 'PENDING', NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-8. Topic 15개 (미팅당 3개, CONFIRMED)
INSERT INTO topic (meeting_id, proposed_by, title, description,
                   topic_type, topic_status, like_count, confirm_order,
                   created_at, updated_at)
SELECT m.meeting_id, u.user_id,
       '약속' || row_number() OVER (ORDER BY m.meeting_id, t.topic_idx) || '_토픽' || t.topic_idx,
       '약속' || row_number() OVER (ORDER BY m.meeting_id, t.topic_idx) || '_토픽' || t.topic_idx || ' 설명',
       'FREE', 'CONFIRMED', 0, t.topic_idx,
       NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
CROSS JOIN (SELECT generate_series(1, 3) AS topic_idx) t
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL
ORDER BY m.meeting_id, t.topic_idx;

-- A-9. TopicAnswer 15건 (is_submitted = TRUE)
INSERT INTO topic_answer (topic_id, user_id, content, is_submitted, created_at, updated_at)
SELECT tp.topic_id, u.user_id,
       tp.title || ' 답변',
       TRUE,
       NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
JOIN topic tp ON tp.meeting_id = m.meeting_id
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-10. TopicRetrospectiveSummary 15건
INSERT INTO topic_retrospective_summary (topic_id, summary, created_at, updated_at)
SELECT tp.topic_id,
       tp.title || ' 요약',
       NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
JOIN topic tp ON tp.meeting_id = m.meeting_id
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-11. PersonalMeetingRetrospective 5건
INSERT INTO personal_meeting_retrospective (meeting_id, user_id, created_at, updated_at)
SELECT m.meeting_id, u.user_id, NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-12. RetrospectiveChangedThought 15건 (회고당 토픽 3개)
INSERT INTO retrospective_changed_thought (
    topic_id, personal_meeting_retrospective_id,
    key_issue, pre_opinion, post_opinion,
    created_at, updated_at)
SELECT tp.topic_id, pmr.personal_meeting_retrospective_id,
       '핵심 쟁점 ' || tp.title,
       '사전 의견 ' || tp.title,
       '사후 의견 ' || tp.title,
       NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
JOIN topic tp ON tp.meeting_id = m.meeting_id
JOIN personal_meeting_retrospective pmr ON pmr.meeting_id = m.meeting_id AND pmr.user_id = u.user_id
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-13. RetrospectiveFreeText 5건 (회고당 1건)
INSERT INTO retrospective_free_text (personal_meeting_retrospective_id, title, content, created_at, updated_at)
SELECT pmr.personal_meeting_retrospective_id,
       '전반적인 감상',
       '약속' || pmr.personal_meeting_retrospective_id || ': 배운 점이 많았습니다.',
       NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
JOIN personal_meeting_retrospective pmr ON pmr.meeting_id = m.meeting_id AND pmr.user_id = u.user_id
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- A-14. RetrospectiveOthersPerspective 5건 (회고당 1건, 각 미팅의 첫 번째 토픽)
INSERT INTO retrospective_others_perspective (
    personal_meeting_retrospective_id, topic_id, meeting_member_id,
    opinion_content, impressive_reason,
    created_at, updated_at)
SELECT pmr.personal_meeting_retrospective_id,
       first_topic.topic_id,
       mm.meeting_member_id,
       '멤버 의견 - ' || first_topic.title,
       '인상적이었던 이유 - ' || first_topic.title,
       NOW(), NOW()
FROM users u
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id
JOIN personal_meeting_retrospective pmr ON pmr.meeting_id = m.meeting_id AND pmr.user_id = u.user_id
JOIN meeting_member mm ON mm.meeting_id = m.meeting_id AND mm.user_id = u.user_id
JOIN LATERAL (
    SELECT topic_id, title
    FROM topic
    WHERE meeting_id = m.meeting_id
    ORDER BY confirm_order ASC
    LIMIT 1
) first_topic ON TRUE
WHERE u.user_email = 'perf@dokdok.com' AND u.deleted_at IS NULL;

-- ══════════════════════════════════════════════════════════════
-- [B] 배경 유저 200명 — PERF-INVITE-TEST 모임에 볼륨 추가
--
-- 타임라인 쿼리 분석:
--   READING_RECORD       : personal_book_id + user_id 필터 → 테스트 유저만
--   PERSONAL_RETROSPECTIVE: user_id + gathering_id 필터  → 테스트 유저만
--   PRE_OPINION          : gathering_id + book_id 필터,
--                          topic_answer.user_id = :userId → 테스트 유저 답변만
--                          (배경 미팅이 많을수록 meeting 스캔 범위 증가)
--   MEETING_RETROSPECTIVE: gathering_id + book_id 필터
--                          → 805 미팅 × 3 토픽 요약 처리 (5 → 805)
--
-- 배경 유저는 별도 gathering 없이 PERF-INVITE-TEST에 미팅만 추가
-- ══════════════════════════════════════════════════════════════

-- B-1. 배경 유저 200명
INSERT INTO users (nickname, user_email, kakao_id, created_at, updated_at)
SELECT '배경유저' || i, 'bg_user_' || i || '@dokdok.com', 9100000000 + i, NOW(), NOW()
FROM generate_series(1, 200) AS i;

-- B-2. Meeting 800개 (배경 유저당 4개, 모두 PERF-INVITE-TEST + Book_A)
--      MEETING_RETROSPECTIVE 쿼리: gathering_id=PERF-INVITE-TEST, book_id=Book_A → 총 805 미팅
INSERT INTO meeting (gathering_id, book_id, meeting_leader_id, meeting_name, meeting_status,
                     meeting_start_date, meeting_end_date, created_at, updated_at)
SELECT g.gathering_id, b.book_id, u.user_id,
       '배경약속_' || i || '_' || m_idx, 'CONFIRMED',
       NOW() - ((i * 4 + m_idx) * INTERVAL '1 day'),
       NOW() - ((i * 4 + m_idx) * INTERVAL '1 day') + INTERVAL '2 hours',
       NOW(), NOW()
FROM generate_series(1, 200) AS i
JOIN users u ON u.user_email = 'bg_user_' || i || '@dokdok.com' AND u.deleted_at IS NULL
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN book b ON b.isbn = 'PERF-ISBN-A'
CROSS JOIN generate_series(1, 4) AS m_idx;

-- B-3. Topic 2400개 (배경 미팅당 3개, CONFIRMED)
INSERT INTO topic (meeting_id, proposed_by, title, description,
                   topic_type, topic_status, like_count, confirm_order,
                   created_at, updated_at)
SELECT m.meeting_id, u.user_id,
       '배경토픽_' || i || '_' || t_idx,
       '배경토픽 설명 ' || i || '_' || t_idx,
       'FREE', 'CONFIRMED', 0, t_idx,
       NOW(), NOW()
FROM generate_series(1, 200) AS i
JOIN users u ON u.user_email = 'bg_user_' || i || '@dokdok.com' AND u.deleted_at IS NULL
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id AND m.meeting_leader_id = u.user_id
CROSS JOIN generate_series(1, 3) AS t_idx;

-- B-4. TopicRetrospectiveSummary 2400건
--      MEETING_RETROSPECTIVE 쿼리 JOIN 대상: 15 → 2,415건
INSERT INTO topic_retrospective_summary (topic_id, summary, created_at, updated_at)
SELECT tp.topic_id, tp.title || ' 요약', NOW(), NOW()
FROM generate_series(1, 200) AS i
JOIN users u ON u.user_email = 'bg_user_' || i || '@dokdok.com' AND u.deleted_at IS NULL
JOIN gathering g ON g.invitation_link = 'PERF-INVITE-TEST'
JOIN meeting m ON m.gathering_id = g.gathering_id AND m.meeting_leader_id = u.user_id
JOIN topic tp ON tp.meeting_id = m.meeting_id;
