# XRail Project 개발 결과 요약 (2026-02-05)

## 1. 핵심 문제 해결 (Critical Issues & Fixes)

*   **좌석 중복 및 호차 간 데이터 공유 문제**
    *   **문제**: 1호차의 특정 좌석 예약 시 다른 호차의 동일 번호 좌석까지 비활성화되거나, 구간 예약 데이터가 꼬이는 현상 발생.
    *   **해결**: 
        *   `DataInitializer` 로직을 개편하여 호차별 물리적 좌석 ID를 완벽히 분리 (1A, 1B, 1C, 1D 현실적 배치).
        *   `SeatRepository`에 **비관적 락(Pessimistic Write Lock)**을 적용하여 트랜잭션 레벨에서 동시 접근 차단.
        *   프론트엔드 인덱스 대신 백엔드 DB의 `RouteStation` 시퀀스를 직접 조회하여 비트마스킹 연산의 정확성 보장.

*   **결제 페이지 무한 로딩 (흰 화면) 오류**
    *   **문제**: 장바구니에서 결제 진입 시 `state` 데이터 구조 차이로 인한 `ReferenceError` 및 렌더링 중단.
    *   **해결**: `PaymentPage.tsx` 상단에 방어 로직(Safety Check)을 추가하고, 옵셔널 체이닝을 통해 누락된 정보가 있어도 기본 정보(예약번호, 금액)는 안전하게 표시되도록 수정.

*   **비회원 예매 로직 정합성**
    *   **문제**: 비회원 토큰 발행 후 사용자 식별 실패 및 비밀번호 유효성 불일치.
    *   **해결**: 비회원 비밀번호를 **6자리**로 강화하고, 결제 성공 후 비밀번호 재확인을 거쳐 **상세 예매 정보와 AccessCode를 노출**하는 완벽한 보안 플로우 구축.

---

## 2. 주요 기능 고도화 및 UI/UX 개선

*   **비회원 예매 프로세스 전면 개편**
    *   로그인 페이지의 비회원 탭을 제거하고, **좌석 선택 후 정보 입력 모달**이 뜨는 자연스러운 흐름으로 변경.
    *   비회원 조회는 메인 화면의 '나의 예약' 메뉴에서만 수행하도록 통합.

*   **권한 기반 기능 제어 (장바구니)**
    *   비회원은 장바구니 기능을 이용할 수 없도록 헤더 메뉴 및 결제 페이지 버튼을 숨김 처리 (회원 전용 기능으로 확립).

*   **실시간 좌석 상태 동기화**
    *   `SeatService`가 DB뿐만 아니라 **Redis의 실시간 점유 상태**를 조회하여, 결제 진행 중(`PENDING`)인 좌석도 즉시 시트맵에서 비활성화(`pointer-events: none`).

*   **거리 기반 요금 및 시간 자동 산출**
    *   `ScheduleService`에서 역간 누적 거리(`cumulativeDistance`)를 계산하여 km당 운임 및 실제 소요 시간을 API 응답에 포함.

---

## 3. 운영 및 디버깅 도구 추가

*   **데이터 강제 초기화 API (`/api/admin/reset-data`)**
    *   서버 재시작 없이도 DB를 깨끗하게 밀고 기초 데이터를 재생성할 수 있는 관리자용 엔드포인트 구축.

## 4. 기술 스택 현황
- **Concurrency**: Redis Lua Script (Atomic 선점) + JPA Pessimistic Lock.
- **Security**: JWT (Subject: LoginId or AccessCode), 6-digit Guest PW.
- **Frontend**: React 19, Modal-based Flow, CSS-based interaction block.
