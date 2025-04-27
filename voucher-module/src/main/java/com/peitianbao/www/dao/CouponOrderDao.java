package com.peitianbao.www.dao;

import com.peitianbao.www.model.CouponOrder;

import java.util.List;

/**
 * @author leg
 */
public interface CouponOrderDao {
    /**
     * 创建秒杀订单
     * @param order 秒杀订单对象
     * @return 是否成功
     */
    boolean createCouponOrder(CouponOrder order);

    /**
     * 根据订单ID查询秒杀订单
     * @param orderId 订单ID
     * @return 秒杀订单对象
     */
    CouponOrder getCouponOrderInfo(String orderId);

    /**
     * 查询用户对某个秒杀活动的参与次数
     * @param userId   用户ID
     * @param couponId 秒杀活动ID
     * @return 参与次数
     */
    int countUserParticipation(Integer userId, Integer couponId);

    /**
     * 查询用户的秒杀订单记录
     * @param userId 用户ID
     * @return 秒杀订单列表
     */
    List<CouponOrder> getUserCouponOrders(Integer userId);

    /**
     * 得到参与某秒杀活动的所有用户id
     * @param couponId 秒杀活动id
     * @return 用户id
     */
    List<CouponOrder> getCouponUsersId(Integer couponId);
}
