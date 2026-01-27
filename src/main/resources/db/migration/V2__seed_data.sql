/* ========================================================
   [XRail] Data Seeding (Dummy Data)
   Target: MySQL 8.0
   Description: 기초 데이터 (역, 노선, 열차, 좌석, 스케줄, 테스트유저)
   ======================================================== */

-- 1. Stations (주요 역 4개)
INSERT INTO stations (name, created_at, updated_at) VALUES ('서울', NOW(), NOW()); -- ID: 1
INSERT INTO stations (name, created_at, updated_at) VALUES ('대전', NOW(), NOW()); -- ID: 2
INSERT INTO stations (name, created_at, updated_at) VALUES ('동대구', NOW(), NOW()); -- ID: 3
INSERT INTO stations (name, created_at, updated_at) VALUES ('부산', NOW(), NOW()); -- ID: 4

-- 2. Routes (경부선)
INSERT INTO routes (name, created_at, updated_at) VALUES ('경부선', NOW(), NOW()); -- ID: 1

-- 3. Route Stations (경부선 순서 및 거리 정의)
-- 순서(Index)가 중요합니다. 서울(0) -> 대전(1) -> 동대구(2) -> 부산(3)
INSERT INTO route_stations (route_id, station_id, station_sequence, cumulative_distance, created_at, updated_at)
VALUES
(1, 1, 0, 0.0, NOW(), NOW()),   -- 서울 (0km)
(1, 2, 1, 150.0, NOW(), NOW()), -- 대전 (150km)
(1, 3, 2, 250.0, NOW(), NOW()), -- 동대구 (250km)
(1, 4, 3, 400.0, NOW(), NOW()); -- 부산 (400km)

-- 4. Trains (KTX 2대)
INSERT INTO trains (train_number, train_type, created_at, updated_at)
VALUES ('KTX-101', 'KTX', NOW(), NOW()); -- ID: 1

INSERT INTO trains (train_number, train_type, created_at, updated_at)
VALUES ('KTX-205', 'KTX_SANCHEON', NOW(), NOW()); -- ID: 2

-- 5. Carriages (1호차)
-- 편의상 1호차만 생성합니다.
INSERT INTO carriages (train_id, carriage_number, seat_count) VALUES (1, 1, 20); -- ID: 1 (KTX-101 1호차)
INSERT INTO carriages (train_id, carriage_number, seat_count) VALUES (2, 1, 20); -- ID: 2 (KTX-205 1호차)

-- 6. Seats (1호차 1A ~ 5D, 총 20석)
-- KTX-101 (ID:1)의 1호차 (ID:1) 좌석 생성
INSERT INTO seats (carriage_id, seat_number) VALUES
(1, '1A'), (1, '1B'), (1, '1C'), (1, '1D'),
(1, '2A'), (1, '2B'), (1, '2C'), (1, '2D'),
(1, '3A'), (1, '3B'), (1, '3C'), (1, '3D'),
(1, '4A'), (1, '4B'), (1, '4C'), (1, '4D'),
(1, '5A'), (1, '5B'), (1, '5C'), (1, '5D');
-- (Seat ID: 1 ~ 20)

-- 7. Schedules (운행 스케줄)
-- 주의: 테스트를 위해 날짜를 '2026-01-01' 로 고정합니다. API 호출 시 이 날짜를 사용하세요.
-- 서울(09:00) -> 부산(12:00) / KTX-101 / 경부선
INSERT INTO schedules (route_id, train_id, departure_date, departure_time, arrival_time, created_at, updated_at)
VALUES (1, 1, '2026-01-01', '09:00:00', '12:00:00', NOW(), NOW()); -- ID: 1

-- 서울(13:00) -> 부산(16:00) / KTX-205 / 경부선
INSERT INTO schedules (route_id, train_id, departure_date, departure_time, arrival_time, created_at, updated_at)
VALUES (1, 2, '2026-01-01', '13:00:00', '16:00:00', NOW(), NOW()); -- ID: 2


-- 8. Test User (Member)
-- ID: tester / PW: password123! (BCrypt Encoded)
INSERT INTO users (dtype, role, created_at, updated_at)
VALUES ('Member', 'MEMBER', NOW(), NOW()); -- User ID: 1

INSERT INTO members (user_id, login_id, password, name, email, phone, birth_date, social_provider)
VALUES (
    1,
    'tester',
    '$2a$10$4.vM9/K/..passwordhash..', -- 실제 해시값은 복잡하므로 아래 값 사용 권장
    '테스트유저',
    'test@xrail.com',
    '010-1234-5678',
    '19900101',
    'NONE'
);

-- 비밀번호 업데이트 (BCrypt: password123!)
UPDATE members
SET password = '$2a$10$N.zmdr9k7uOCQb376ye6tup1x.k1J.Zd/xX6W/j.s.j.H.u.K.u.K'
WHERE user_id = 1;