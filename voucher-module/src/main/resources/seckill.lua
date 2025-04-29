-- KEYS[1] = 库存 Key: coupon_stock:{couponId}
-- KEYS[2] = 用户参与次数 Key: coupon:user:participate:{userId}:{couponId}

-- ARGV[1] = 该优惠券用户最大限购数量 maxPerUser
-- ARGV[2] = 用户 ID
-- ARGV[3] = 优惠券 ID

local stockKey = KEYS[1]
local userParticipationKey = KEYS[2]

local maxPerUser = tonumber(ARGV[1])
local userId = tonumber(ARGV[2])
local couponId = tonumber(ARGV[3])

-- 1. 检查用户是否已达限购次数
local userParticipated = redis.call('GET', userParticipationKey)

if userParticipated and tonumber(userParticipated) >= maxPerUser then
    return '-1' -- -1 表示 "用户已达限购"
end

-- 2. 如果没有参与记录，则自增并设置过期时间
redis.call('INCR', userParticipationKey)
redis.call('EXPIRE', userParticipationKey, 86400) -- 当天有效

-- 3. 扣减库存
local stock = redis.call('GET', stockKey)
if not stock or tonumber(stock) <= 0 then
    return '-2' -- -2 表示 "库存不足"
end

redis.call('DECR', stockKey)
redis.call('EXPIRE', stockKey, 86400)

-- 4. 返回成功信号
return '1'