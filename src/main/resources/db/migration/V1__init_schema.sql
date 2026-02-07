/* ========================================================
   [XRail] Schema Initialization
   DBMS: MySQL 8.0
   Strategy: JPA Joined Inheritance for Users
   ======================================================== */

-- 1. Users (Base Table)
-- 상속 관계의 부모 테이블 (공통 정보)
CREATE TABLE users (
    user_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    dtype       VARCHAR(31)  NOT NULL, -- 구분자 (Member/NonMember)
    role        VARCHAR(20)  NOT NULL, -- ROLE_MEMBER, ROLE_ADMIN
    created_at  DATETIME(6),
    updated_at  DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Members (Sub Table)
-- 회원 정보 (Users와 1:1, PK 공유)
CREATE TABLE members (
    user_id         BIGINT NOT NULL PRIMARY KEY,
    login_id        VARCHAR(50),
    password        VARCHAR(255) NOT NULL,
    name            VARCHAR(50)  NOT NULL,
    email           VARCHAR(100) NOT NULL,
    phone           VARCHAR(20),
    birth_date      VARCHAR(8),
    social_provider VARCHAR(20),
    social_id       VARCHAR(100),
    CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT uk_members_login_id UNIQUE (login_id),
    CONSTRAINT uk_members_email UNIQUE (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. NonMembers (Sub Table)
-- 비회원 정보 (Users와 1:1, PK 공유)
CREATE TABLE non_members (
    user_id     BIGINT NOT NULL PRIMARY KEY,
    name        VARCHAR(50) NOT NULL,
    phone       VARCHAR(20) NOT NULL,
    password    VARCHAR(4)  NOT NULL, -- 숫자 4자리
    access_code VARCHAR(10) NOT NULL, -- 예매 조회용 랜덤 코드
    CONSTRAINT fk_non_members_user FOREIGN KEY (user_id) REFERENCES users (user_id),
    CONSTRAINT uk_non_members_access_code UNIQUE (access_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- [Index] 비회원 조회 속도 최적화
CREATE INDEX idx_non_member_access ON non_members(access_code);


-- 4. Stations (Master Data)
CREATE TABLE stations (
    station_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL,
    created_at DATETIME(6),
    updated_at DATETIME(6),
    CONSTRAINT uk_stations_name UNIQUE (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 5. Routes (Master Data)
CREATE TABLE routes (
    route_id   BIGINT AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(50) NOT NULL, -- 경부선, 호남선
    created_at DATETIME(6),
    updated_at DATETIME(6)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 6. Route Stations (Relation)
-- 노선에 속한 역 정보 (순서, 거리)
CREATE TABLE route_stations (
    route_station_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id            BIGINT NOT NULL,
    station_id          BIGINT NOT NULL,
    station_sequence    INT NOT NULL, -- 0, 1, 2... (Bitmask Index)
    cumulative_distance DOUBLE NOT NULL,
    created_at          DATETIME(6),
    updated_at          DATETIME(6),
    CONSTRAINT fk_rs_route FOREIGN KEY (route_id) REFERENCES routes (route_id),
    CONSTRAINT fk_rs_station FOREIGN KEY (station_id) REFERENCES stations (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- [Index] 노선별 역 순서 조회
CREATE INDEX idx_route_station_seq ON route_stations(route_id, station_sequence);


-- 7. Trains (Physical)
CREATE TABLE trains (
    train_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_number VARCHAR(255) NOT NULL, -- 101, 205
    train_type   VARCHAR(20)  NOT NULL, -- KTX, MUGUNGHWA
    created_at   DATETIME(6),
    updated_at   DATETIME(6),
    CONSTRAINT uk_trains_number UNIQUE (train_number)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 8. Carriages (Physical)
CREATE TABLE carriages (
    carriage_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    train_id        BIGINT NOT NULL,
    carriage_number INT NOT NULL, -- 1호차
    seat_count      INT NOT NULL, -- 40석
    CONSTRAINT fk_carriages_train FOREIGN KEY (train_id) REFERENCES trains (train_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 9. Seats (Physical)
CREATE TABLE seats (
    seat_id     BIGINT AUTO_INCREMENT PRIMARY KEY,
    carriage_id BIGINT NOT NULL,
    seat_number VARCHAR(5) NOT NULL, -- 1A, 1B
    CONSTRAINT fk_seats_carriage FOREIGN KEY (carriage_id) REFERENCES carriages (carriage_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 10. Schedules (Time)
CREATE TABLE schedules (
    schedule_id    BIGINT AUTO_INCREMENT PRIMARY KEY,
    route_id       BIGINT NOT NULL,
    train_id       BIGINT NOT NULL,
    departure_date DATE NOT NULL,
    departure_time TIME NOT NULL,
    arrival_time   TIME NOT NULL,
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    CONSTRAINT fk_schedules_route FOREIGN KEY (route_id) REFERENCES routes (route_id),
    CONSTRAINT fk_schedules_train FOREIGN KEY (train_id) REFERENCES trains (train_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- [Index] 스케줄 검색 성능 최적화 (날짜+노선+시간)
CREATE INDEX idx_schedule_search ON schedules(departure_date, route_id, departure_time);


-- 11. Reservations (Transaction Header)
CREATE TABLE reservations (
    reservation_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id        BIGINT NOT NULL,
    status         VARCHAR(20) NOT NULL, -- PENDING, PAID
    total_price    BIGINT NOT NULL,
    reserved_at    DATETIME(6),
    created_at     DATETIME(6),
    updated_at     DATETIME(6),
    CONSTRAINT fk_reservations_user FOREIGN KEY (user_id) REFERENCES users (user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- 12. Tickets (Transaction Detail)
CREATE TABLE tickets (
    ticket_id         BIGINT AUTO_INCREMENT PRIMARY KEY,
    reservation_id    BIGINT NOT NULL,
    schedule_id       BIGINT NOT NULL,
    seat_id           BIGINT NOT NULL,
    start_station_id  BIGINT NOT NULL,
    end_station_id    BIGINT NOT NULL,
    start_station_idx INT NOT NULL, -- 구간 시작 인덱스
    end_station_idx   INT NOT NULL, -- 구간 종료 인덱스
    price             BIGINT NOT NULL,
    status            VARCHAR(20) NOT NULL,
    created_at        DATETIME(6),
    updated_at        DATETIME(6),
    CONSTRAINT fk_tickets_reservation FOREIGN KEY (reservation_id) REFERENCES reservations (reservation_id),
    CONSTRAINT fk_tickets_schedule FOREIGN KEY (schedule_id) REFERENCES schedules (schedule_id),
    CONSTRAINT fk_tickets_seat FOREIGN KEY (seat_id) REFERENCES seats (seat_id),
    CONSTRAINT fk_tickets_start FOREIGN KEY (start_station_id) REFERENCES stations (station_id),
    CONSTRAINT fk_tickets_end FOREIGN KEY (end_station_id) REFERENCES stations (station_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- [Index] 구간 중복 체크용 핵심 인덱스
CREATE INDEX idx_ticket_segment ON tickets(schedule_id, seat_id);