-- KEYS[1] = 库存 Key: coupon_stock:{couponId}
-- KEYS[2] = 用户参与次数 Key: coupon:user:participate:{userId}:{couponId}

-- ARGV[1] = 最大限购数量 maxPerUser
-- ARGV[2] = 用户 ID
-- ARGV[3] = 优惠券 ID

local stockKey = KEYS[1]
local userParticipationKey = KEYS[2]

local maxPerUser = tonumber(ARGV[1])
local currentCount = tonumber(redis.call('GET', userParticipationKey)) or 0

-- 判断是否超限
if currentCount >= maxPerUser then
    return '-1'
end

-- 自增参与次数，并且不设 TTL，永久记录
redis.call('SET', userParticipationKey, currentCount + 1)

local stock = redis.call('GET', stockKey)
if not stock or tonumber(stock) <= 0 then
    return '-2'
end

redis.call('DECR', stockKey)

return '1'