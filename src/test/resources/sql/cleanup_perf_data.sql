-- =============================================================
-- 성능 테스트 더미 데이터 정리 스크립트 (순수 SQL)
-- =============================================================

-- 역순 삭제 (FK 제약 순서)

-- 테스트 유저 회고 관련 (bg 유저는 회고 데이터 없음)
DELETE FROM retrospective_changed_thought
WHERE personal_meeting_retrospective_id IN (
    SELECT pmr.personal_meeting_retrospective_id
    FROM personal_meeting_retrospective pmr
    JOIN users u ON u.user_id = pmr.user_id
    WHERE u.user_email = 'perf@dokdok.com'
);

DELETE FROM retrospective_free_text
WHERE personal_meeting_retrospective_id IN (
    SELECT pmr.personal_meeting_retrospective_id
    FROM personal_meeting_retrospective pmr
    JOIN users u ON u.user_id = pmr.user_id
    WHERE u.user_email = 'perf@dokdok.com'
);

DELETE FROM retrospective_others_perspective
WHERE personal_meeting_retrospective_id IN (
    SELECT pmr.personal_meeting_retrospective_id
    FROM personal_meeting_retrospective pmr
    JOIN users u ON u.user_id = pmr.user_id
    WHERE u.user_email = 'perf@dokdok.com'
);

DELETE FROM personal_meeting_retrospective
WHERE user_id IN (
    SELECT user_id FROM users
    WHERE user_email = 'perf@dokdok.com'
);

-- 토픽 요약, 답변, 토픽 (PERF-INVITE-TEST 내 전체 — bg 유저 미팅 포함)
DELETE FROM topic_retrospective_summary
WHERE topic_id IN (
    SELECT tp.topic_id
    FROM topic tp
    JOIN meeting m ON m.meeting_id = tp.meeting_id
    JOIN gathering g ON g.gathering_id = m.gathering_id
    WHERE g.invitation_link = 'PERF-INVITE-TEST'
);

DELETE FROM topic_answer
WHERE user_id IN (
    SELECT user_id FROM users
    WHERE user_email = 'perf@dokdok.com'
);

DELETE FROM topic
WHERE meeting_id IN (
    SELECT m.meeting_id
    FROM meeting m
    JOIN gathering g ON g.gathering_id = m.gathering_id
    WHERE g.invitation_link = 'PERF-INVITE-TEST'
);

-- 미팅 멤버, 미팅
DELETE FROM meeting_member
WHERE meeting_id IN (
    SELECT m.meeting_id
    FROM meeting m
    JOIN gathering g ON g.gathering_id = m.gathering_id
    WHERE g.invitation_link = 'PERF-INVITE-TEST'
);

DELETE FROM meeting
WHERE gathering_id IN (
    SELECT gathering_id FROM gathering
    WHERE invitation_link = 'PERF-INVITE-TEST'
);

-- 독서 기록, personal_book
DELETE FROM personal_reading_record
WHERE user_id IN (
    SELECT user_id FROM users
    WHERE user_email = 'perf@dokdok.com' OR user_email LIKE 'bg_user_%@dokdok.com'
);

DELETE FROM personal_book
WHERE user_id IN (
    SELECT user_id FROM users
    WHERE user_email = 'perf@dokdok.com' OR user_email LIKE 'bg_user_%@dokdok.com'
);

-- 모임
DELETE FROM gathering
WHERE invitation_link = 'PERF-INVITE-TEST';

-- 책
DELETE FROM book WHERE isbn IN ('PERF-ISBN-A', 'PERF-ISBN-B');

-- 유저 (hard delete — 테스트 DB이므로)
DELETE FROM users
WHERE user_email = 'perf@dokdok.com'
   OR user_email LIKE 'bg_user_%@dokdok.com';
