package com.peitianbao.www.controller;


import com.peitianbao.www.exception.UserException;
import com.peitianbao.www.model.dto.UsersDTO;
import com.peitianbao.www.model.po.UsersPO;
import com.peitianbao.www.service.UserService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.ResponseUtil;
import com.peitianbao.www.util.token.JwtUtil;
import com.peitianbao.www.util.token.RedisUtil;
import redis.clients.jedis.Jedis;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class UserController {
    @Autowired
    UserService userService;

    /**
     * 用户登录
     */
    @RequestMapping(value="/login",methodType = RequestMethod.POST)
    public void userLogin(@MyRequestBody UsersPO usersPo ,HttpServletResponse resp) throws IOException{
        String userAccount=usersPo.getUserAccount();
        String userPassword=usersPo.getUserPassword();

        if(userAccount==null||userPassword==null){
            throw new UserException("[400] 用户的账号或密码不能为空");
        }

        UsersDTO user = userService.userLogin(userAccount,userPassword);
        if(user==null){
            throw new UserException("[401] 用户的密码或账户输入有误");
        }

        int userId=user.getUserId();
        //使用JwtUtil生成Token和Refresh Token
        String accessToken = JwtUtil.generateAccessToken(userId);
        String refreshToken = JwtUtil.generateRefreshToken(userId);

        //使用Redis存储Token
        try (Jedis jedis = RedisUtil.getJedis()) {
            jedis.setex("user:access:" + userId, (JwtUtil.EXPIRATION_TIME / 1000), accessToken);
            jedis.setex("user:refresh:" + userId, (JwtUtil.REFRESH_EXPIRATION_TIME / 1000), refreshToken);
        }

        //返回成功响应
        Map<String, Object> responseData = Map.of(
                "userId", userId,
                "token", accessToken,
                "refreshToken", refreshToken
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 用户注册
     */
    @RequestMapping(value = "/register", methodType = RequestMethod.POST)
    public void userRegister(@MyRequestBody UsersPO usersPo, HttpServletResponse resp) throws IOException {
        //获取请求参数
        String userAccount = usersPo.getUserAccount();
        String userPassword = usersPo.getUserPassword();
        String userName = usersPo.getUserName();

        //调用Service层进行注册
        boolean result = userService.userRegister(userName,userPassword,userAccount);

        if (result) {
            //注册成功，返回成功响应
            Map<String, Object> responseData = Map.of(
                    "message", "用户成功注册"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            //注册失败，抛出异常
            throw new UserException("[400] 用户的注册账号或名字已存在");
        }
    }

    /**
     * 用户更新
     */
    @RequestMapping(value = "/update", methodType = RequestMethod.POST)
    public void userUpdate(@MyRequestBody UsersPO usersPo, HttpServletRequest request, HttpServletResponse resp) throws IOException {
        //获取请求参数
        Integer userId = (Integer) request.getAttribute("userId");
        String userAccount = usersPo.getUserAccount();
        String userPassword = usersPo.getUserPassword();
        String userName = usersPo.getUserName();

        //调用Service层进行注册
        boolean result = userService.userUpdate(userId,userName,userPassword,userAccount);

        if (result) {
            //更新成功，返回成功响应
            Map<String, Object> responseData = Map.of(
                    "message", "用户成功更新"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            //更新失败，抛出异常
            throw new UserException("[400] 用户的更新账号或名字已存在");
        }
    }

    /**
     * 用户注销
     */
    @RequestMapping(value = "/delete", methodType = RequestMethod.DELETE)
    public void deleteUser(HttpServletRequest request, HttpServletResponse resp) throws IOException {
        //从请求上下文中获取userId
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new UserException("[401] 无法获取用户信息");
        }

        //调用Service层进行注销
        boolean result = userService.userDelete(userId);

        if (result) {
            //注销成功，返回成功响应
            Map<String, Object> responseData = Map.of(
                    "message", "用户注销成功"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            throw new UserException("[500] 用户注销失败");
        }
    }

    /**
     * 查询用户信息
     */
    @RequestMapping(value = "/info", methodType = RequestMethod.POST)
    public void getUserInfo(@MyRequestBody UsersDTO usersDto, HttpServletResponse resp) throws IOException {
        //从请求上下文中获取userId
        Integer userId = usersDto.getUserId();
        if (userId == null) {
            throw new UserException("[401] 无法获取用户id");
        }
        //调用Service层查询用户信息
        UsersDTO user = userService.showUserInfo(userId);

        if (user != null) {
            //查询成功，返回成功响应
            Map<String, Object> responseData = Map.of(
                    "message", "用户信息查询成功",
                    "userAccount", user.getUserAccount(),
                    "userName", user.getUserName(),
                    "followers", user.getFollowers(),
                    "FollowingShops", user.getFollowingShops(),
                    "FollowingUsers", user.getFollowingUsers()
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        } else {
            //信息不存在
            throw new UserException("[404] 用户信息不存在");
        }
    }

    /**
     * 用户刷新Token
     */
    @RequestMapping(value = "/userRefreshToken", methodType = RequestMethod.POST)
    public void refreshToken(HttpServletRequest request, HttpServletResponse response){
        Integer userId = (Integer) request.getAttribute("userId");
        if (userId == null) {
            throw new UserException("[401] 身份未认证或 Token 失效");
        }

        try (Jedis jedis = RedisUtil.getJedis()) {
            String redisKey = "user:refresh:" + userId;
            String cachedRefreshToken = jedis.get(redisKey);

            String refreshToken = request.getHeader("Authorization").replace("Bearer ", "").trim();

            if (!refreshToken.equals(cachedRefreshToken)) {
                throw new UserException("[401] Refresh Token已失效或不存在");
            }

            String newAccessToken = JwtUtil.generateAccessToken(userId);

            jedis.setex("user:access:" + userId, (JwtUtil.EXPIRATION_TIME / 1000), newAccessToken);

            Map<String, Object> responseData = Map.of(
                    "message", "Token 刷新成功",
                    "data", Map.of("token", newAccessToken)
            );
            ResponseUtil.sendSuccessResponse(response, responseData);
        } catch (Exception e) {
            throw new UserException("[500] 服务器错误" );
        }
    }
}
