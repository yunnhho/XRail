import http from 'k6/http';
import { check, sleep } from 'k6';
import { randomIntBetween } from 'https://jslib.k6.io/k6-utils/1.2.0/index.js';

// 테스트 설정
export let options = {
  stages: [
    { duration: '10s', target: 100 }, // 10초 동안 유저 100명까지 증가 (Warming up)
    { duration: '30s', target: 500 }, // 30초 동안 유저 500명 유지 (Load)
    { duration: '10s', target: 0 },   // 10초 동안 종료
  ],
  thresholds: {
    http_req_duration: ['p(95)<2000'], // 95%의 요청이 2초 이내여야 함
    http_req_failed: ['rate<0.1'],     // 에러율 10% 미만 (대기열 차단 제외)
  },
};

const BASE_URL = 'http://localhost:8088/api';

export default function () {
  // 1. 유저 ID 랜덤 선정 (DB에 생성한 1~1000번 유저)
  const userId = randomIntBetween(1, 1000);
  const params = {
    headers: {
      'Content-Type': 'application/json',
      'X-User-Id': `${userId}`, // 테스트용 헤더
    },
  };

  // ==========================================
  // Step 1: 대기열 진입 (Queue Token)
  // ==========================================
  let queueRes = http.post(`${BASE_URL}/queue/token`, null, params);

  check(queueRes, {
    'Queue Join Status is 200': (r) => r.status === 200,
  });

  // ==========================================
  // Step 2: 대기열 폴링 (Polling)
  // ==========================================
  let isAllowed = false;
  let attempt = 0;

  // 최대 30초 동안 폴링
  while (!isAllowed && attempt < 30) {
    let statusRes = http.get(`${BASE_URL}/queue/status`, params);

    // 응답 확인
    if (statusRes.status === 200) {
      const body = JSON.parse(statusRes.body);
      if (body.data.status === 'ACTIVE') {
        isAllowed = true;
        break;
      }
    }

    sleep(1); // 1초 대기 후 재시도
    attempt++;
  }

  // ==========================================
  // Step 3: 예매 요청 (Reservation)
  // ==========================================
  if (isAllowed) {
    const payload = JSON.stringify({
      scheduleId: 1,      // 서울-부산 09:00
      seatId: 1,          // 1A 좌석 (경쟁 심화)
      startStationId: 1,  // 서울
      endStationId: 2,    // 대전 (0구간)
      startStationIdx: 0,
      endStationIdx: 1,
      price: 10000
    });

    let reserveRes = http.post(`${BASE_URL}/reservations`, payload, params);

    // 결과 확인
    check(reserveRes, {
      'Reservation Success (200)': (r) => r.status === 200,
      'Already Reserved (Biz Error)': (r) => r.status === 400 || r.status === 500, // 비즈니스 예외는 보통 400/500으로 옴
      'Access Denied (403)': (r) => r.status === 403,
    });
  }
}