package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.dao.CouponDao;
import com.peitianbao.www.exception.VoucherException;
import com.peitianbao.www.model.Coupon;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.GsonFactory;
import com.peitianbao.www.util.token.RedisUtil;

import java.time.LocalDateTime;
import java.util.*;

/**
 * @author leg
 */
@Service
public class CouponService {

    @Autowired
    private CouponDao couponDao;

    private static final String COUPON_INFO_PREFIX = "coupon:info:";
    private static final String COUPON_ACTIVITIES_STATUS_PREFIX = "coupon:activities:status";

    //基础缓存过期时间
    private static final int BASE_CACHE_EXPIRE_SECONDS = 3600;

    //随机化缓存过期时间的最大偏移量
    private static final int RANDOM_EXPIRE_OFFSET = 300;

    //缓存空值的过期时间
    private static final int EMPTY_CACHE_EXPIRE_SECONDS = 300;

    private final Gson gson = GsonFactory.getGSON();

    /**
     * 创建秒杀活动
     */
    public boolean createCoupon(Coupon coupon) {
        boolean result = couponDao.createCoupon(coupon);
        if (result) {
            RedisUtil.delete(COUPON_ACTIVITIES_STATUS_PREFIX);
            return true;
        } else {
            throw new VoucherException("创建秒杀活动失败");
        }
    }

    /**
     * 查询秒杀活动信息
     */
    public Coupon getCouponInfo(Integer couponId) {
        String cacheKey = COUPON_INFO_PREFIX + couponId;

        String cachedCoupon = RedisUtil.get(cacheKey);
        if (cachedCoupon != null) {
            if ("NOT_EXISTS".equals(cachedCoupon)) {
                throw new VoucherException("活动不存在");
            }
            return gson.fromJson(cachedCoupon, Coupon.class);
        }

        Coupon coupon = couponDao.getCouponInfo(couponId);
        if (coupon == null) {
            RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
            throw new VoucherException("活动不存在");
        }

        // 设置缓存，增加随机化过期时间
        RedisUtil.set(cacheKey, gson.toJson(coupon), getRandomExpireTime());
        return coupon;
    }

    /**
     * 扣减现有库存
     */
    public boolean lowNowCount(Integer couponId) {
        boolean result = couponDao.lowNowCount(couponId);
        if (result) {
            updateCouponCache(couponId);
            return true;
        } else {
            throw new VoucherException("减少现有库存失败");
        }
    }

    /**
     * 回滚库存数量
     */
    public boolean rollbackCoupon(Integer couponId) {
        boolean result = couponDao.rollbackCoupon(couponId);
        if (result) {
            updateCouponCache(couponId);
            return true;
        } else {
            throw new VoucherException("回滚库存失败");
        }
    }

    /**
     * 获取按状态分类的秒杀活动列表
     */
    public List<Coupon> getCouponActivitiesByStatus(String sortType) {
        String cacheKey = COUPON_ACTIVITIES_STATUS_PREFIX + ":" + sortType;

        String cachedActivities = RedisUtil.get(cacheKey);
        if (cachedActivities != null) {
            if ("NOT_EXISTS".equals(cachedActivities)) {
                throw new VoucherException("未查询到该状态的秒杀活动");
            }
            return gson.fromJson(cachedActivities, new TypeToken<List<Coupon>>() {}.getType());
        }

        List<Coupon> allCoupons = couponDao.getAllCoupons();
        if (allCoupons == null || allCoupons.isEmpty()) {
            RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
            throw new VoucherException("查询秒杀活动信息为空");
        }

        Map<String, List<Coupon>> classifiedCoupons = classifyCouponsByStatus(allCoupons);

        List<Coupon> result = classifiedCoupons.getOrDefault(sortType, Collections.emptyList());

        if (result.isEmpty()) {
            RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
            throw new VoucherException("未查询到该状态的秒杀活动");
        }

        RedisUtil.set(cacheKey, gson.toJson(result), getRandomExpireTime());
        return result;
    }

    /**
     * 按状态分类秒杀活动
     */
    private Map<String, List<Coupon>> classifyCouponsByStatus(List<Coupon> coupons) {
        Map<String, List<Coupon>> result = new HashMap<>();
        result.put("upcoming", new ArrayList<>());
        result.put("ongoing", new ArrayList<>());
        result.put("expired", new ArrayList<>());

        LocalDateTime now = LocalDateTime.now();

        for (Coupon coupon : coupons) {
            if (now.isBefore(coupon.getStartTime())) {
                result.get("upcoming").add(coupon);
            } else if (now.isAfter(coupon.getStartTime()) && now.isBefore(coupon.getEndTime())) {
                result.get("ongoing").add(coupon);
            } else {
                result.get("expired").add(coupon);
            }
        }

        return result;
    }

    /**
     * 更新单个秒杀活动的缓存
     */
    private void updateCouponCache(Integer couponId) {
        String cacheKey = COUPON_INFO_PREFIX + couponId;
        Coupon updatedCoupon = couponDao.getCouponInfo(couponId);
        if (updatedCoupon != null) {
            RedisUtil.set(cacheKey, gson.toJson(updatedCoupon), getRandomExpireTime());
        } else {
            RedisUtil.delete(cacheKey);
        }
    }

    /**
     * 获取随机化的缓存过期时间
     */
    private int getRandomExpireTime() {
        return BASE_CACHE_EXPIRE_SECONDS + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
    }
}