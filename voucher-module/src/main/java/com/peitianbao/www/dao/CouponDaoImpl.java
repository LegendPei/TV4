package com.peitianbao.www.dao;

import com.peitianbao.www.exception.VoucherException;
import com.peitianbao.www.model.Coupon;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Dao;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Dao
public class CouponDaoImpl implements CouponDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    @LoggingFramework.Log
    public boolean createCoupon(Coupon coupon) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("couponType", coupon.getCouponType());
            params.put("couponName", coupon.getCouponName());
            params.put("discountAmount", coupon.getDiscountAmount());
            params.put("minSpend", coupon.getMinSpend());
            params.put("totalStock", coupon.getTotalStock());
            params.put("availableStock", coupon.getTotalStock());
            params.put("startTime", coupon.getStartTime());
            params.put("endTime", coupon.getEndTime());
            params.put("maxPerUser", coupon.getMaxPerUser());

            LoggingFramework.info("尝试插入秒杀活动：" + coupon.getCouponName());
            int result = sqlSession.executeUpdate("CouponMapper.insertCoupon", params);
            LoggingFramework.info("秒杀活动插入成功：" + coupon.getCouponName());
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入活动失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new VoucherException("插入活动失败", e);
        }
    }

    @Override
    public Coupon getCouponInfo(Integer couponId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("couponId", couponId);

            LoggingFramework.info("尝试查询活动 ID：" + couponId);
            Coupon coupon = sqlSession.executeQueryForObject("CouponMapper.selectCouponById", params, Coupon.class);

            if (coupon != null) {
                LoggingFramework.info("查询到活动：" + coupon.getCouponName());
            } else {
                LoggingFramework.warning("未找到活动 ID：" + couponId);
            }
            return coupon;
        } catch (Exception e) {
            LoggingFramework.severe("查询活动失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public boolean lowNowCount(Integer couponId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("couponId", couponId);

            LoggingFramework.info("尝试更新活动卷现有数数：couponId = " + couponId);
            int result = sqlSession.executeUpdate("CouponMapper.lowNowCount", params);
            if (result > 0) {
                LoggingFramework.info("活动卷现有数更新成功：couponId = " + couponId);
            } else {
                LoggingFramework.warning("活动卷现有数更新失败：couponId = " + couponId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("活动卷现有数更新失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new VoucherException("活动卷现有数更新失败", e);
        }
    }

    @Override
    public boolean rollbackCoupon(Integer couponId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("couponId", couponId);

            LoggingFramework.info("尝试回滚活动卷现有数数：couponId = " + couponId);
            int result = sqlSession.executeUpdate("CouponMapper.rollbackCoupon", params);
            if (result > 0) {
                LoggingFramework.info("活动卷现有数回滚成功：couponId = " + couponId);
            } else {
                LoggingFramework.warning("活动卷现有数回滚失败：couponId = " + couponId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("活动卷现有数回滚失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new VoucherException("活动卷现有数回滚失败", e);
        }
    }

    @Override
    public List<Coupon> getAllCoupons() {
        try {
            Map<String, Object> params = new HashMap<>();
            return sqlSession.executeQueryForList("CouponMapper.getAllCoupons",params,Coupon.class);
        } catch (Exception e) {
            LoggingFramework.severe("搜索活动失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }
}
