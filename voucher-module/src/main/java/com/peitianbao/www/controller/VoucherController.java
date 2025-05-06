package com.peitianbao.www.controller;

import com.google.gson.Gson;
import com.peitianbao.www.exception.VoucherException;
import com.peitianbao.www.model.Coupon;
import com.peitianbao.www.model.CouponOrder;
import com.peitianbao.www.model.SortRequest;
import com.peitianbao.www.service.CouponOrderService;
import com.peitianbao.www.service.CouponService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.GsonFactory;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.ResponseUtil;
import com.peitianbao.www.util.VoucherId;
import com.peitianbao.www.util.token.RedisUtil;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
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

    private final Gson gson = GsonFactory.getGSON();

    //Redis缓存临时订单
    private static final String TEMP_ORDER_PREFIX = "temp:order:";
    private static final int TEMP_ORDER_EXPIRE_SECONDS = 60 * 5;

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
                    "message", "秒杀活动成功创建"
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
     * 获取参与某活动的所有用户ID列表
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

    /**
     * 用户点击秒杀
     */
    @RequestMapping(value = "/secKill", methodType = RequestMethod.POST)
    public void secKill(@MyRequestBody CouponOrder request, HttpServletResponse resp) {
        Integer couponId = request.getCouponId();
        Integer userId = request.getUserId();

        if (couponId == null || userId == null) {
            throw new VoucherException("[400] 参数缺失");
        }

        LoggingFramework.info("收到秒杀请求：userId=" + userId + ", couponId=" + couponId);

        Coupon coupon = couponService.getCouponInfo(couponId);
        if (coupon == null) {
            throw new VoucherException("[401] 秒杀活动不存在");
        }

        LoggingFramework.info("当前活动限购数量：" + coupon.getMaxPerUser());

        String stockKey = "coupon_stock:" + couponId;
        String userParticipationKey = "coupon:user:participate:" + userId + ":" + couponId;

        List<String> keys = Arrays.asList(stockKey, userParticipationKey);
        List<String> args = Arrays.asList(
                String.valueOf(coupon.getMaxPerUser()),
                String.valueOf(userId),
                String.valueOf(couponId)
        );

        String luaScript = RedisUtil.loadLuaScript("seckill.lua");

        try (Jedis jedis = RedisUtil.getJedis()) {
            Object rawResult = jedis.eval(luaScript, keys, args);
            String result = rawResult.toString();

            switch (result) {
                case "-1":
                    throw new VoucherException("[401] 您已达到购买上限");
                case "-2":
                    throw new VoucherException("[402] 库存不足");
                case "1":
                    break;
                default:
                    throw new VoucherException("[500] 未知错误：" + result);
            }

            long orderId = VoucherId.voucherId("coupon");
            CouponOrder order = new CouponOrder(orderId, couponId, userId);

            RedisUtil.set(TEMP_ORDER_PREFIX + orderId, gson.toJson(order), TEMP_ORDER_EXPIRE_SECONDS);

            Map<String, Object> responseData = Map.of(
                    "message", "抢购成功，请前往支付",
                    "orderId", orderId,
                    "couponId", couponId,
                    "userId", userId
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("秒杀异常：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new VoucherException("[500] 秒杀异常：" + e.getMessage());
        }
    }

    /**
     * 用户点击已支付
     */
    @RequestMapping(value = "/confirmPayment", methodType = RequestMethod.POST)
    public void confirmPayment(@MyRequestBody CouponOrder request, HttpServletResponse resp) throws IOException {
        Long orderId = request.getOrderId();
        Integer couponId = request.getCouponId();
        Integer userId = request.getUserId();

        //检查Redis中是否存在该订单
        String tempOrderKey = TEMP_ORDER_PREFIX + orderId;
        String cachedOrderJson = RedisUtil.get(tempOrderKey);
        if (cachedOrderJson == null) {
            throw new VoucherException("[404] 订单不存在");
        }

        try {
            CouponOrder tempOrder = gson.fromJson(cachedOrderJson, CouponOrder.class);
            if (!tempOrder.getUserId().equals(userId) || !tempOrder.getCouponId().equals(couponId)) {
                throw new VoucherException("[403] 订单信息不匹配");
            }

            boolean createSuccess = couponOrderService.createCouponOrder(tempOrder.getOrderId(), tempOrder.getCouponId(), tempOrder.getUserId());
            boolean result = couponService.lowNowCount(tempOrder.getCouponId());

            if (!createSuccess || !result) {
                throw new VoucherException("[500] 订单创建失败");
            }

            RedisUtil.delete(tempOrderKey);

            Map<String, Object> responseData = Map.of(
                    "message", "支付成功",
                    "orderId", orderId
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("支付确认异常：" + e.getMessage());
            throw new VoucherException("[500] 支付确认失败：" + e.getMessage());
        }
    }

    /**
     * 用户点击不想要了
     */
    @RequestMapping(value = "/cancelPayment", methodType = RequestMethod.POST)
    public void cancelPayment(@MyRequestBody CouponOrder request, HttpServletResponse resp) throws IOException {
        long orderId = request.getOrderId();
        Integer couponId = request.getCouponId();
        Integer userId = request.getUserId();

        //检查Redis中是否有这个订单
        String tempOrderKey = TEMP_ORDER_PREFIX + orderId;
        String cachedOrderJson = RedisUtil.get(tempOrderKey);
        if (cachedOrderJson == null) {
            throw new VoucherException("[404] 该订单已处理");
        }

        CouponOrder tempOrder = gson.fromJson(cachedOrderJson, CouponOrder.class);
        if (!tempOrder.getUserId().equals(userId) || !tempOrder.getCouponId().equals(couponId)) {
            throw new VoucherException("[403] 订单信息不匹配");
        }

        boolean rollbackSuccess = couponService.rollbackCoupon(couponId);
        if (!rollbackSuccess) {
            throw new VoucherException("[500] 库存回滚失败");
        }

        RedisUtil.delete(tempOrderKey);

        Map<String, Object> responseData = Map.of(
                "message", "取消订单成功"
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }
}
