-- =============================================================
-- 약속 상세 조회 성능 테스트용 더미 데이터 삽입 스크립트
--
-- 생성 데이터:
--   리더 유저 1명 (perf_meeting@dokdok.com)
--   멤버 유저 14명 (pm_member_1@dokdok.com ~ pm_member_14@dokdok.com)
--   Book 1권 (MEET-PERF-ISBN)
--   Gathering 1개 (MEET-PERF-INVITE)
--   gathering_member 15건
--   meeting 1건 (CONFIRMED)
--   meeting_member 15건
--
-- 목표: 15명 멤버 대상 presigned URL 동기/비동기 성능 비교
-- =============================================================

-- 1. 리더 유저
INSERT INTO users (nickname, user_email, kakao_id, profile_image_url, created_at, updated_at)
VALUES ('약속성능테스트리더', 'perf_meeting@dokdok.com', 9200000001, 'profile/perf_meeting.jpg', NOW(), NOW());

-- 2. 멤버 유저 14명
INSERT INTO users (nickname, user_email, kakao_id, profile_image_url, created_at, updated_at)
SELECT
    '약속성능멤버' || i,
    'pm_member_' || i || '@dokdok.com',
    9200000001 + i,
    'profile/pm_member_' || i || '.jpg',
    NOW(), NOW()
FROM generate_series(1, 14) AS i;

-- 3. 책
INSERT INTO book (book_name, author, publisher, isbn, thumbnail, created_at, updated_at)
VALUES ('약속성능테스트책', '테스트 저자', '테스트 출판사', 'MEET-PERF-ISBN', 'https://example.com/meet_perf.jpg', NOW(), NOW());

-- 4. 모임
INSERT INTO gathering (gathering_leader_id, gathering_name, description, invitation_link, gathering_status, created_at, updated_at)
SELECT user_id, '약속성능테스트모임', '약속 성능 테스트를 위한 모임', 'MEET-PERF-INVITE', 'ACTIVE', NOW(), NOW()
FROM users
WHERE user_email = 'perf_meeting@dokdok.com' AND deleted_at IS NULL;

-- 5. gathering_member 15건 (리더 포함)
INSERT INTO gathering_member (gathering_id, user_id, is_favorite, member_status, role, joined_at, updated_at)
SELECT g.gathering_id, u.user_id, FALSE, 'ACTIVE', 'MEMBER', NOW(), NOW()
FROM gathering g
JOIN users u ON (u.user_email = 'perf_meeting@dokdok.com'
             OR u.user_email LIKE 'pm_member_%@dokdok.com')
AND u.deleted_at IS NULL
WHERE g.invitation_link = 'MEET-PERF-INVITE';

-- 6. meeting 1건 (CONFIRMED)
INSERT INTO meeting (gathering_id, book_id, meeting_leader_id, meeting_name, meeting_status,
                     meeting_start_date, meeting_end_date, created_at, updated_at)
SELECT g.gathering_id, b.book_id, u.user_id,
       '약속성능테스트약속', 'CONFIRMED',
       NOW() + INTERVAL '7 days',
       NOW() + INTERVAL '7 days' + INTERVAL '2 hours',
       NOW(), NOW()
FROM gathering g
JOIN book b ON b.isbn = 'MEET-PERF-ISBN'
JOIN users u ON u.user_email = 'perf_meeting@dokdok.com' AND u.deleted_at IS NULL
WHERE g.invitation_link = 'MEET-PERF-INVITE';

-- 7. meeting_member 15건 (리더 포함)
INSERT INTO meeting_member (meeting_id, user_id, meeting_role, attendance_status, joined_at, updated_at)
SELECT m.meeting_id, u.user_id,
       CASE WHEN u.user_email = 'perf_meeting@dokdok.com' THEN 'LEADER' ELSE 'MEMBER' END,
       'PENDING', NOW(), NOW()
FROM meeting m
JOIN gathering g ON g.gathering_id = m.gathering_id
JOIN users u ON (u.user_email = 'perf_meeting@dokdok.com'
             OR u.user_email LIKE 'pm_member_%@dokdok.com')
AND u.deleted_at IS NULL
WHERE g.invitation_link = 'MEET-PERF-INVITE';
