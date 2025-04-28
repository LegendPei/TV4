package com.peitianbao.www.dao;

import com.peitianbao.www.exception.VoucherException;
import com.peitianbao.www.model.CouponOrder;
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
public class CouponOrderDaoImpl implements CouponOrderDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    @LoggingFramework.Log
    public boolean createCouponOrder(CouponOrder order) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", order.getOrderId());
            params.put("couponId", order.getCouponId());
            params.put("userId", order.getUserId());

            LoggingFramework.info("尝试插入秒杀订单：" + order.getOrderId());
            int result = sqlSession.executeUpdate("CouponOrderMapper.insertCouponOrder", params);
            LoggingFramework.info("秒杀订单插入成功：" + order.getOrderId());
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入订单失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new VoucherException("插入订单失败", e);
        }
    }

    @Override
    public CouponOrder getCouponOrderInfo(long orderId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("orderId", orderId);

            LoggingFramework.info("尝试查询订单 ID：" + orderId);

            return sqlSession.executeQueryForObject("CouponOrderMapper.selectCouponOrderById", params, CouponOrder.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询订单失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public int countUserParticipation(Integer userId, Integer couponId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);
            params.put("couponId", couponId);

            LoggingFramework.info("尝试统计用户参与次数：用户ID=" + userId + ", 活动ID=" + couponId);

            Integer count = sqlSession.executeQueryForObject("CouponOrderMapper.countUserParticipation", params, Integer.class);
            return count != null ? count : 0;
        } catch (Exception e) {
            LoggingFramework.severe("统计用户参与次数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return 0;
        }
    }

    @Override
    public List<CouponOrder> getUserCouponOrders(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试查询用户的订单，ID：" + userId);

            return sqlSession.executeQueryForList("CouponOrderMapper.getUserCouponOrders", params, CouponOrder.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询用户订单失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<CouponOrder> getCouponUsersId(Integer couponId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("couponId", couponId);

            LoggingFramework.info("尝试查询参与该活动的用户id,活动ID：" + couponId);

            return sqlSession.executeQueryForList("CouponOrderMapper.getCouponUsersId", params, CouponOrder.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询参与该活动的用户id失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }
}
