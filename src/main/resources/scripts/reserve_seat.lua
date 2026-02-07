-- KEYS[1]: sch:{scheduleId}:seat:{seatId}
-- ARGV[1]: 요청한 구간 마스크 (Java에서 생성한 Long값)
-- ARGV[2]: TTL (초)

local current = redis.call('GET', KEYS[1])
if (current == false) then current = 0 else current = tonumber(current) end

local mask = tonumber(ARGV[1])
local ttl = tonumber(ARGV[2])

-- 겹치는 비트가 있는지 확인 (AND 연산)
if (bit.band(current, mask) == 0) then
    -- 비트 점유 (OR 연산)
    local nextState = bit.bor(current, mask)
    redis.call('SET', KEYS[1], nextState)
    redis.call('EXPIRE', KEYS[1], ttl)
    return 1 -- 성공
else
    return 0 -- 실패 (이미 예약됨)
end