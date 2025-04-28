package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.dao.CouponOrderDao;
import com.peitianbao.www.exception.VoucherException;
import com.peitianbao.www.model.CouponOrder;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.GsonFactory;
import com.peitianbao.www.util.VoucherId;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author leg
 */
@Service
public class CouponOrderService {

    @Autowired
    private CouponOrderDao couponOrderDao;

    private static final String ORDER_INFO_PREFIX = "order:info:";
    private static final String USER_COUPON_ORDERS_PREFIX = "user:coupon:orders:";
    private static final String COUPON_USERS_ID_PREFIX = "coupon:users:id:";

    //基础缓存过期时间
    private static final int BASE_CACHE_EXPIRE_SECONDS = 3600;

    //随机化缓存过期时间的最大偏移量
    private static final int RANDOM_EXPIRE_OFFSET = 300;

    //缓存空值的过期时间
    private static final int EMPTY_CACHE_EXPIRE_SECONDS = 300;

    private final Gson gson = GsonFactory.getGSON();

    /**
     * 创建秒杀订单
     */
    public boolean createCouponOrder(Integer couponId, Integer userId) {
        long orderId = VoucherId.voucherId("coupon");
        CouponOrder order = new CouponOrder(orderId, couponId, userId);

        boolean result = couponOrderDao.createCouponOrder(order);
        if (result) {
            String cacheKey = ORDER_INFO_PREFIX + orderId;
            RedisUtil.set(cacheKey, gson.toJson(order), getRandomExpireTime());
            return true;
        } else {
            throw new VoucherException("创建订单失败");
        }
    }

    /**
     * 查询秒杀订单信息
     */
    public CouponOrder getCouponOrderInfo(long orderId) {
        String cacheKey = ORDER_INFO_PREFIX + orderId;

        String cachedOrder = RedisUtil.get(cacheKey);
        if (cachedOrder != null) {
            if ("NOT_EXISTS".equals(cachedOrder)) {
                throw new VoucherException("未查询到该订单信息");
            }
            return gson.fromJson(cachedOrder, CouponOrder.class);
        }

        CouponOrder order = couponOrderDao.getCouponOrderInfo(orderId);
        if (order == null) {
            RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
            throw new VoucherException("未查询到该订单信息");
        }

        RedisUtil.set(cacheKey, gson.toJson(order), getRandomExpireTime());
        return order;
    }

    /**
     * 统计用户参与活动次数
     */
    public int countUserParticipation(Integer userId, Integer couponId) {
        return couponOrderDao.countUserParticipation(userId, couponId);
    }

    /**
     * 获取用户的秒杀订单列表
     */
    public List<CouponOrder> getUserCouponOrders(Integer userId) {
        String cacheKey = USER_COUPON_ORDERS_PREFIX + userId;

        String cachedOrders = RedisUtil.get(cacheKey);
        if (cachedOrders != null) {
            if ("NOT_EXISTS".equals(cachedOrders)) {
                throw new VoucherException("用户订单为空");
            }
            return gson.fromJson(cachedOrders, new TypeToken<List<CouponOrder>>() {}.getType());
        }

        List<CouponOrder> orders = couponOrderDao.getUserCouponOrders(userId);
        if (orders == null || orders.isEmpty()) {
            RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
            throw new VoucherException("用户订单为空");
        }

        RedisUtil.set(cacheKey, gson.toJson(orders), getRandomExpireTime());
        return orders;
    }

    /**
     * 获取参与某活动的所有用户 ID 列表
     */
    public List<Integer> getCouponUsersId(Integer couponId) {
        String cacheKey = COUPON_USERS_ID_PREFIX + couponId;

        String cachedUserIds = RedisUtil.get(cacheKey);
        if (cachedUserIds != null) {
            if ("NOT_EXISTS".equals(cachedUserIds)) {
                throw new VoucherException("没有用户参与该活动");
            }
            return gson.fromJson(cachedUserIds, new TypeToken<List<Integer>>() {}.getType());
        }

        List<CouponOrder> result = couponOrderDao.getCouponUsersId(couponId);
        if (result == null || result.isEmpty()) {
            RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
            throw new VoucherException("没有用户参与该活动");
        }

        List<Integer> userIds = result.stream()
                .map(CouponOrder::getUserId)
                .collect(Collectors.toList());

        RedisUtil.set(cacheKey, gson.toJson(userIds), getRandomExpireTime());
        return userIds;
    }

    /**
     * 获取随机化的缓存过期时间
     */
    private int getRandomExpireTime() {
        return BASE_CACHE_EXPIRE_SECONDS + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
    }
}