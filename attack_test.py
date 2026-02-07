import requests
import threading
import time

# ==========================================
# [Configuration]
# ==========================================
BASE_URL = "http://localhost:8088/api"
LOGIN_ID = "tester"          # V2__seed_data.sql에 있는 유저
PASSWORD = "password123!"    # 초기 비밀번호
SCHEDULE_ID = 1              # KTX-101 (서울->부산)
SEAT_ID = 1                  # 1호차 1A 좌석 (이거 하나 두고 싸움)
START_STATION_ID = 1         # 서울
END_STATION_ID = 4           # 부산
REQ_COUNT = 100              # 공격 횟수

# 결과 집계용 변수
success_count = 0
fail_count = 0
lock = threading.Lock()

def get_access_token():
    """로그인 후 JWT 토큰 획득"""
    url = f"{BASE_URL}/auth/login"
    payload = {
        "loginId": LOGIN_ID,
        "password": PASSWORD
    }
    try:
        res = requests.post(url, json=payload)
        if res.status_code == 200:
            token = res.json()['accessToken']
            print(f"[Login Success] Token obtained.")
            return token
        else:
            print(f"[Login Failed] {res.text}")
            exit(1)
    except Exception as e:
        print(f"[Error] Server not running? {e}")
        exit(1)

def request_reservation(token, user_idx):
    """예매 요청 함수 (쓰레드용)"""
    global success_count, fail_count

    url = f"{BASE_URL}/reservations"
    headers = {"Authorization": f"Bearer {token}"}
    payload = {
        "scheduleId": SCHEDULE_ID,
        "seatId": SEAT_ID,
        "startStationId": START_STATION_ID,
        "endStationId": END_STATION_ID,
        "startStationIdx": 0, # 서울
        "endStationIdx": 3,   # 부산
        "price": 59800
    }

    try:
        res = requests.post(url, json=payload, headers=headers)
        with lock:
            if res.status_code == 200:
                print(f"✅ User-{user_idx}: 예매 성공! (ID: {res.json()['data']})")
                success_count += 1
            else:
                # print(f"❌ User-{user_idx}: 실패 ({res.json()['error']['message']})")
                fail_count += 1
    except Exception as e:
        with lock:
            print(f"⚠️ User-{user_idx}: 에러 ({e})")
            fail_count += 1

def start_attack():
    print(f"🚀 [Start] {REQ_COUNT}명이 동시에 1개의 좌석을 노립니다...")

    # 1. 토큰 발급 (편의상 1개의 토큰으로 테스트하지만, 서버는 동시 요청으로 인식함)
    token = get_access_token()

    threads = []

    # 2. 쓰레드 생성 및 실행
    for i in range(REQ_COUNT):
        t = threading.Thread(target=request_reservation, args=(token, i))
        threads.append(t)

    start_time = time.time()

    for t in threads:
        t.start()

    for t in threads:
        t.join()

    end_time = time.time()

    # 3. 결과 출력
    print("\n" + "="*40)
    print(f"⏱️  소요 시간: {end_time - start_time:.2f}초")
    print(f"🏆 예매 성공: {success_count} 건 (정답: 1)")
    print(f"💥 예매 실패: {fail_count} 건 (정답: 99)")
    print("="*40)

    if success_count == 1:
        print("🎉 GREAT! 동시성 제어가 완벽합니다.")
    else:
        print("😱 FAILED! 중복 예매가 발생했습니다.")

if __name__ == "__main__":
    start_attack()