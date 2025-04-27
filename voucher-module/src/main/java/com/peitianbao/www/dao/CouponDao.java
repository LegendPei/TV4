package com.peitianbao.www.dao;

import com.peitianbao.www.model.Coupon;

import java.util.List;

/**
 * @author leg
 */
public interface CouponDao {
    /**
     * 创建秒杀活动
     * @param coupon 要创建的秒杀活动
     * @return 是否创建成功
     */
    boolean createCoupon(Coupon coupon);

    /**
     * 得到秒杀活动信息
     * @param couponId 秒杀活动id
     * @return 该秒杀活动
     */
    Coupon getCouponInfo(Integer couponId);

    /**
     * 降低现有库存一个
     * @param couponId 秒杀活动id
     * @return 是否减少成功
     */
    boolean lowNowCount(Integer couponId);

    /**
     * 回滚库存数量
     * @param couponId 秒杀活动id
     * @return 是否回滚成功
     */
    boolean rollbackCoupon(Integer couponId);

    /**
     * 得到所有秒杀活动
     * @return 活动集合
     */
    List<Coupon> getAllCoupons();
}
