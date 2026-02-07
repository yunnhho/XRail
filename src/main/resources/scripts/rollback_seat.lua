-- KEYS[1]: sch:{scheduleId}:seat:{seatId}
-- ARGV[1]: 해제할 구간 마스크

local current = redis.call('GET', KEYS[1])
if (current == false) then return 0 end

local mask = tonumber(ARGV[1])

-- 안전한 롤백: 현재 상태에서 해당 마스크 비트만 0으로 제거 (AND NOT)
-- bit.bnot(mask)는 mask의 비트를 반전시킴 (0->1, 1->0)
local nextState = bit.band(tonumber(current), bit.bnot(mask))

redis.call('SET', KEYS[1], nextState)
return 1