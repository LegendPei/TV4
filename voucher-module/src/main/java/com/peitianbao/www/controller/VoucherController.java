package com.peitianbao.www.controller;

import com.peitianbao.www.exception.VoucherException;
import com.peitianbao.www.model.Coupon;
import com.peitianbao.www.model.CouponOrder;
import com.peitianbao.www.model.SortRequest;
import com.peitianbao.www.service.CouponOrderService;
import com.peitianbao.www.service.CouponService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.ResponseUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class VoucherController {
    @Autowired
    CouponOrderService couponOrderService;

    @Autowired
    CouponService couponService;

    /**
     * 创建秒杀活动
     */
    @RequestMapping(value = "/createSecKill", methodType = RequestMethod.POST)
    public void createSecKill(@MyRequestBody Coupon coupon, HttpServletResponse resp) throws IOException{
        if(coupon.getShopId()<200000||coupon.getShopId()>=300000){
            throw new VoucherException("[401] 商铺id有误");
        }
        boolean result = couponService.createCoupon(coupon);
        if(result){
            Map<String, Object> responseData = Map.of(
                    "message", "商铺成功注册"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new VoucherException("[400] 创建秒杀活动失败");
        }
    }

    /**
     * 查询秒杀活动信息
     */
    @RequestMapping(value = "/getSecKillInfo", methodType = RequestMethod.POST)
    public void getSecKillInfo(@MyRequestBody Coupon coupon, HttpServletResponse resp) throws IOException{
        Integer couponId = coupon.getCouponId();
        if(couponId<300000){
            throw new VoucherException("[401] 查询秒杀活动id有误");
        }
        Coupon secKill=couponService.getCouponInfo(couponId);
        if(secKill==null){
            throw new VoucherException("[401] 秒杀活动信息为空");
        }
        Map<String, Object> responseData = Map.of(
                "couponName", secKill.getCouponName(),
                "couponType", secKill.getCouponType(),
                "discountAmount", secKill.getDiscountAmount(),
                "minSpend", secKill.getMinSpend(),
                "totalStock", secKill.getTotalStock(),
                "availableStock", secKill.getAvailableStock(),
                "startTime", secKill.getStartTime(),
                "endTime", secKill.getEndTime(),
                "maxPerUser",secKill.getMaxPerUser(),
                "shopId", secKill.getShopId()
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 获取按状态分类的秒杀活动列表
     */
    @RequestMapping(value = "/getALLSecKillInfo", methodType = RequestMethod.POST)
    public void getAllSecKillInfo(@MyRequestBody SortRequest sortRequest, HttpServletResponse resp) throws IOException{
        String sortType = sortRequest.getSortType();
        List<Coupon> coupons=couponService.getCouponActivitiesByStatus(sortType);
        if(coupons==null){
            throw new VoucherException("[401] 该状态的活动列表为空");
        }
        Map<String, Object> responseData = Map.of(
                "message", "商铺查询成功",
                "data", coupons
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 获取用户的秒杀订单列表
     */
    @RequestMapping(value = "/getUserCouponOrders", methodType = RequestMethod.POST)
    public void getUserCouponOrders(@MyRequestBody CouponOrder couponOrder, HttpServletResponse resp) throws IOException{
        Integer userId = couponOrder.getUserId();
        if(userId<100000||userId>200000){
            throw new VoucherException("[401] 传入数据有误");
        }
        List<CouponOrder> couponOrders=couponOrderService.getUserCouponOrders(userId);
        if(couponOrders==null){
            throw new VoucherException("[401] 暂无用户参与秒杀活动记录");
        }
        Map<String, Object> responseData = Map.of(
                "message", "商铺查询成功",
                "data", couponOrders
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 获取参与某活动的所有用户 ID 列表
     */
    @RequestMapping(value = "/getCouponUsersId", methodType = RequestMethod.POST)
    public void getCouponUsersId(@MyRequestBody CouponOrder couponOrder, HttpServletResponse resp) throws IOException{
        Integer couponId = couponOrder.getCouponId();
        if(couponId<300000){
            throw new VoucherException("[401] 查询秒杀活动id有误");
        }
        List<Integer>useIds=couponOrderService.getCouponUsersId(couponId);
        if(useIds==null){
            throw new VoucherException("[401] 暂无用户参与该秒杀活动");
        }
        Map<String, Object> responseData = Map.of(
                "message", "商铺查询成功",
                "data", useIds
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 查询秒杀订单信息
     */
    @RequestMapping(value = "/getCouponOrderInfo", methodType = RequestMethod.POST)
    public void getCouponOrderInfo(@MyRequestBody CouponOrder couponOrder, HttpServletResponse resp) throws IOException{
        long orderId = couponOrder.getOrderId();
        CouponOrder order=couponOrderService.getCouponOrderInfo(orderId);
        if(order==null){
            throw new VoucherException("[401] 暂无该订单信息");
        }
        Integer couponId = order.getCouponId();
        Integer userId = order.getUserId();
        Map<String, Object> responseData = Map.of(
                "message", "商铺查询成功",
                "orderId", orderId,
                "userId", userId,
                "couponId",couponId
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }
}
