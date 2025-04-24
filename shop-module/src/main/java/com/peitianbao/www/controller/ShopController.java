package com.peitianbao.www.controller;

import com.peitianbao.www.exception.ShopException;
import com.peitianbao.www.model.SortRequest;
import com.peitianbao.www.model.dto.ShopsDTO;
import com.peitianbao.www.model.po.ShopsPO;
import com.peitianbao.www.service.ShopService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.ResponseUtil;
import com.peitianbao.www.util.token.JwtUtil;
import com.peitianbao.www.util.token.RedisUtil;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class ShopController {
    @Autowired
    ShopService shopService;

    /**
     * 商铺登录
     */
    @RequestMapping(value = "/login", methodType = RequestMethod.POST)
    public void shopLogin(@MyRequestBody ShopsPO shopsPo, HttpServletResponse resp) throws IOException {
        //获取请求参数
        String shopAccount = shopsPo.getShopAccount();
        String shopPassword = shopsPo.getShopPassword();

        if (shopAccount == null || shopPassword == null) {
            throw new ShopException("[400] 商铺的账号或密码不能为空");
        }

        //调用Service层进行登录验证
        ShopsDTO shop = shopService.shopLogin(shopAccount, shopPassword);
        if (shop == null) {
            throw new ShopException("[401] 商铺的账号或密码错误");
        }

        int shopId = shop.getShopId();

        //使用JwtUtil生成Token和Refresh Token
        String accessToken = JwtUtil.generateAccessToken(shopId);
        String refreshToken = JwtUtil.generateRefreshToken(shopId);

        //使用Redis存储Token
        try (Jedis jedis = RedisUtil.getJedis()) {
            jedis.setex("shop:access:" + shopId, (JwtUtil.EXPIRATION_TIME / 1000), accessToken);
            jedis.setex("shop:refresh:" + shopId, (JwtUtil.REFRESH_EXPIRATION_TIME / 1000), refreshToken);
        }

        // 返回成功响应
        Map<String, Object> responseData = Map.of(
                "shopId", shopId,
                "token", accessToken,
                "refreshToken", refreshToken
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 商铺注册
     * */
    @RequestMapping(value = "/register", methodType = RequestMethod.POST)
    public void shopRegister(@MyRequestBody ShopsPO shopsPo, HttpServletResponse resp) throws IOException {
        //获取请求参数
        String shopAccount = shopsPo.getShopAccount();
        String shopPassword = shopsPo.getShopPassword();
        String shopName = shopsPo.getShopName();
        String shopAddress = shopsPo.getShopAddress();
        String shopInfo = shopsPo.getShopInfo();

        //调用Service层进行注册
        boolean result = shopService.shopRegister(shopAccount, shopPassword, shopName, shopAddress, shopInfo);

        if (result) {
            //注册成功，返回成功响应
            Map<String, Object> responseData = Map.of(
                    "message", "商铺成功注册"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            //注册失败，抛出异常
            throw new ShopException("[400] 商铺的注册账号或名字已存在");
        }
    }

    /**
     * 商铺更新
     * */
    @RequestMapping(value = "/update", methodType = RequestMethod.POST)
    public void shopUpdate(@MyRequestBody ShopsPO shopsPo, HttpServletRequest request, HttpServletResponse resp) throws IOException {
        //获取请求参数
        Integer shopId = (Integer) request.getAttribute("shopId");
        String shopAccount = shopsPo.getShopAccount();
        String shopPassword = shopsPo.getShopPassword();
        String shopName = shopsPo.getShopName();
        String shopAddress = shopsPo.getShopAddress();
        String shopInfo = shopsPo.getShopInfo();

        //调用Service层进行注册
        boolean result = shopService.shopUpdate(shopId,shopAccount, shopPassword, shopName, shopAddress, shopInfo);

        if (result) {
            //更新成功，返回成功响应
            Map<String, Object> responseData = Map.of(
                    "message", "商铺成功更新"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            //更新失败，抛出异常
            throw new ShopException("[400] 商铺的更新账号或名字已存在");
        }
    }

    /**
     * 商铺注销
     */
    @RequestMapping(value = "/delete", methodType = RequestMethod.DELETE)
    public void deleteShop(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        //从请求上下文中获取shopId
        Integer shopId = (Integer) request.getAttribute("shopId");
        if (shopId == null) {
            throw new ShopException("[401] 无法获取商铺信息");
        }

        //调用Service层进行注销
        boolean result = shopService.shopDelete(shopId);

        if (result) {
            //注销成功，返回成功响应
            Map<String, Object> responseData = Map.of(
                   "message", "商铺注销成功"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            throw new ShopException("[500] 商铺注销失败");
        }
    }

    /**
     * 查询商铺信息
     */
    @RequestMapping(value = "/info", methodType = RequestMethod.POST)
    public void getShopInfo(@MyRequestBody ShopsDTO shopsDto, HttpServletResponse resp) throws IOException {
        //从请求上下文中获取shopId
        Integer shopId = shopsDto.getShopId();
        if (shopId == null) {
            throw new ShopException("[401] 无法获取商铺id");
        }
        //调用Service层查询商铺信息
        ShopsDTO shop = shopService.showShopInfo(shopId);

        if (shop != null) {
                //查询成功，返回成功响应
                Map<String, Object> responseData = Map.of(
                        "message", "商铺信息查询成功",
                        "shopInfo", shop.getShopInfo(),
                        "shopAddress", shop.getShopAddress(),
                        "shopAccount", shop.getShopAccount(),
                        "shopFollowers", shop.getShopFollowers(),
                        "shopLikes", shop.getShopLikes(),
                        "shopName",shop.getShopName()
                );
                ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            //商铺信息不存在
            throw new ShopException("[404] 商铺信息不存在");
        }
    }

    /**
     * 查询所有商铺，支持排序
     */
    @RequestMapping(value = "/list", methodType = RequestMethod.POST)
    public void getAllShops(@MyRequestBody SortRequest sortRequest, HttpServletResponse resp) throws IOException {
        String sortType = sortRequest.getSortType();

        List<ShopsDTO> shops = shopService.findAllShops(sortType);

        if(shops!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "商铺查询成功",
                    "data", shops
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else{
            throw new ShopException("[404] 商铺信息不存在");
        }
    }
}
