# [Project Spec] Life-Line: 고속열차 예매 시스템 (Rail-X)

대용량 트래픽 환경에서 데이터 무결성과 시스템 안정성을 보장하는 고성능 열차 예매 시스템 프로젝트입니다.

## 1. 개요 (Overview)
* **프로젝트 명:** High-Concurrency Train Reservation System (Rail-X)
* **핵심 목표:**
    1. **대용량 처리:** 수십만 동시 접속(Thundering Herd) 상황 대응 및 대기열 제어.
    2. **데이터 무결성:** 구간 예매(Segment Booking) 비트마스킹 구현 및 중복 예약 방지.
    3. **보안:** 매크로/암표 방지 및 비회원 개인정보 보호.
* **벤치마킹:** Let's Korail (성능 우위 지향)

---

## 2. 시스템 아키텍처 (System Architecture)
Modular Monolith 구조로 시작하여 향후 MSA 확장이 용이하도록 설계되었습니다.

### 2.1. 트래픽 제어 계층 (Traffic Control Layer)
* **CDN / LB:** 정적 리소스 캐싱 및 L7 로드밸런싱.
* **Waiting Queue (대기열 시스템):**
    * **Tech:** Redis Sorted Set (Score: Timestamp).
    * **Logic:** 접속 시 대기표 발급 → 순번 도달 시 입장 토큰(Access Token) 발급 → 토큰 보유자만 API 호출 허용.

### 2.2. 애플리케이션 계층 (Application Layer)
* **Backend:** Java 21, Spring Boot 3.3.
* **Security:** Spring Security + JWT (Stateless).
* **Async:** Apache Kafka (알림톡, 이메일, 통계 데이터 집계).

### 2.3. 데이터 계층 (Data Layer)
* **Main DB:** MySQL 8.0 (Master-Slave Replication).
* **Cache & Lock:** Redis Cluster (세션, 대기열, 좌석 비트맵, 분산 락).

---

## 3. 데이터베이스 설계 (Database Design)

### 3.1. 핵심 테이블 상세
1. **users (통합 사용자)**
    * `user_id` (PK), `role` (MEMBER/NON_MEMBER), `access_code` (비회원용 랜덤값 + Index).
2. **schedules (운행 스케줄)**
    * `schedule_id` (PK), `route_id`, `train_id`, `departure_date`, `departure_time`.
    * **Partitioning:** `departure_date` 기준 월(Month) 단위 Range Partitioning.
    * **Index:** `(departure_date, route_id, departure_time)` - 조회 최적화.
3. **tickets (티켓 - 동시성 격전지)**
    * `ticket_id` (PK), `reservation_id` (FK), `schedule_id` (FK), `seat_id` (FK).
    * `start_station_idx`, `end_station_idx` (구간 정보).
    * **Constraint:** 애플리케이션 레벨 검증 수행, 일반 인덱스(`schedule_id`, `seat_id`) 사용.

---

## 4. 핵심 비즈니스 로직 (Core Business Logic)

### 4.1. 구간 관리 및 비트마스킹 (Segment Bitmasking)
* **개념:** $N$개의 역 사이 $N-1$개의 구간 관리.
* **매핑 규칙:** * 출발역 Index: $S$, 도착역 Index: $E$.
    * 점유 비트 범위: $S$ ~ $E-1$.
    * (예: 서울(0) → 대전(1) → 동대구(2) 이동 시 0번 비트만 점유)

### 4.2. Redis Lua Script (Atomic Reservation)
* 조회와 수정 사이의 Race Condition 차단.
* **Logic:**
    1. 구간($S \sim E-1$) 마스크 생성.
    2. Redis 좌석 상태(Current) 조회.
    3. `Current & Mask == 0` 검증 (충돌 체크).
    4. 성공 시 `Current | Mask` 저장.

### 4.3. 보상 트랜잭션 (Compensation Logic)
* **시나리오:** Redis 선점 성공 후 DB Insert 실패 시 대응.
* **대응:** 1. `catch` 블록에서 즉시 Redis 비트 롤백(해제).
    2. **Reconciliation (재대조):** 5분 주기 배치로 Redis 비트맵과 DB 데이터 동기화.

---

## 5. 보안 및 부정 예매 방지 (Security & Anti-Fraud)
* **인증:** JWT 기반 Stateless 인증. 비회원은 랜덤코드(10자리)로 토큰 발급.
* **매크로 방어:**
    * IP당 API 호출 제한 (Bucket4j + Redis).
    * 의심 패턴 시 CAPTCHA(슬라이드 퍼즐) 강제.
    * **Honeypot:** 숨겨진 필드 입력 시 즉시 차단.

---

## 6. 기술 스택 (Tech Stack)
| 구분 | 기술 스택 | 버전/비고 |
| :--- | :--- | :--- |
| **Language** | Java | 21 (LTS) |
| **Framework** | Spring Boot | 3.3.x |
| **Database** | MySQL | 8.0 (InnoDB) |
| **Cache/NoSQL** | Redis | 7.x (Cluster Mode) |
| **ORM** | Spring Data JPA | + QueryDSL 5.0 |
| **Message Queue** | Apache Kafka | 3.6.x |
| **Build/Test** | Gradle / JUnit 5 | + k6 (Load Test) |