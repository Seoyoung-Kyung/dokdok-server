-- =============================================================
-- 약속 상세 조회 성능 테스트 더미 데이터 정리 스크립트
-- FK 역순으로 삭제
-- =============================================================

-- meeting_member
DELETE FROM meeting_member
WHERE meeting_id IN (
    SELECT m.meeting_id
    FROM meeting m
    JOIN gathering g ON g.gathering_id = m.gathering_id
    WHERE g.invitation_link = 'MEET-PERF-INVITE'
);

-- meeting
DELETE FROM meeting
WHERE gathering_id IN (
    SELECT gathering_id FROM gathering
    WHERE invitation_link = 'MEET-PERF-INVITE'
);

-- gathering_member
DELETE FROM gathering_member
WHERE gathering_id IN (
    SELECT gathering_id FROM gathering
    WHERE invitation_link = 'MEET-PERF-INVITE'
);

-- gathering
DELETE FROM gathering
WHERE invitation_link = 'MEET-PERF-INVITE';

-- book
DELETE FROM book WHERE isbn = 'MEET-PERF-ISBN';

-- users
DELETE FROM users
WHERE user_email = 'perf_meeting@dokdok.com'
   OR user_email LIKE 'pm_member_%@dokdok.com';
