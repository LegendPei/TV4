package com.peitianbao.www.controller;

import com.peitianbao.www.api.ShopService;
import com.peitianbao.www.api.UserService;
import com.peitianbao.www.exception.FollowException;
import com.peitianbao.www.exception.LikeException;
import com.peitianbao.www.model.Follows;
import com.peitianbao.www.service.FollowService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.ResponseUtil;
import io.seata.core.context.RootContext;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class FollowController {
    @Autowired
    private FollowService followService;

    @DubboReference(serviceName = "userService")
    private UserService userService;

    @DubboReference(serviceName = "shopService")
    private ShopService shopService;

    /**
     * 关注用户
     */
    @RequestMapping(value = "/followUser",methodType = RequestMethod.POST)
    public void followUser(@MyRequestBody Follows follows, HttpServletResponse resp) {
        Integer targetId = follows.getTargetId();
        Integer followerId = follows.getFollowerId();

        if (targetId == null || followerId == null) {
            throw new FollowException("[401] 请求参数有误");
        }

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        try {
            tx.begin(60000, "my_test_tx_group");
            LoggingFramework.info("开始关注用户事务：" + tx.getXid());

            boolean result1 = followService.followUser(targetId, followerId);
            boolean result2 = userService.incrementFollowingUsers(followerId);
            boolean result3 = userService.incrementUserFollowers(targetId);

            if (!result1 || !result2 || !result3) {
                LoggingFramework.warning("关注失败，准备回滚事务");
                tx.rollback();
                throw new FollowException("[400] 关注用户失败");
            }

            LoggingFramework.info("关注成功，提交事务");
            tx.commit();

            Map<String, Object> responseData = Map.of("message", "关注用户成功");
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("发生异常，强制回滚事务: " + e.getMessage());
            try {
                if (tx != null && RootContext.inGlobalTransaction()) {
                    tx.rollback();
                    LoggingFramework.info("关注用户事务已手动回滚");
                }
            } catch (Exception rollbackEx) {
                LoggingFramework.severe("事务回滚失败: " + rollbackEx.getMessage());
            }

            throw new FollowException("[500] 关注用户异常");
        }
    }

    /**
     * 取消关注用户
     */
    @RequestMapping(value = "/unfollowUser",methodType = RequestMethod.POST)
    public void unfollowUser(@MyRequestBody Follows follows, HttpServletResponse resp)throws IOException {
        Integer targetId = follows.getTargetId();
        Integer followerId = follows.getFollowerId();

        if (targetId == null || followerId == null) {
            throw new FollowException("[401] 请求参数有误");
        }

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        try {
            tx.begin(60000, "my_test_tx_group");
            LoggingFramework.info("开始取消关注事务：" + tx.getXid());

            boolean result1 = followService.unfollowUser(targetId, followerId);
            boolean result2 = userService.lowFollowingUsers(followerId);
            boolean result3 = userService.lowUserFollowers(targetId);

            if (!result1 || !result2 || !result3) {
                LoggingFramework.warning("取消关注失败，准备回滚事务");
                tx.rollback();
                throw new FollowException("[400] 取消关注用户失败");
            }

            LoggingFramework.info("取消关注成功，提交事务");
            tx.commit();

            Map<String, Object> responseData = Map.of("message", "取消关注用户成功");
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("发生异常，强制回滚事务: " + e.getMessage());
            try {
                if (tx != null && RootContext.inGlobalTransaction()) {
                    tx.rollback();
                    LoggingFramework.info("取消关注事务已手动回滚");
                }
            } catch (Exception rollbackEx) {
                LoggingFramework.severe("事务回滚失败: " + rollbackEx.getMessage());
            }

            throw new FollowException("[500] 取消关注用户异常：" + e.getMessage());
        }
    }

    /**
     * 关注商铺
     */
    @RequestMapping(value = "/followShop",methodType = RequestMethod.POST)
    public void followShop(@MyRequestBody Follows follows, HttpServletResponse resp)throws IOException {
        Integer targetId = follows.getTargetId();
        Integer followerId = follows.getFollowerId();

        if (targetId == null || followerId == null) {
            throw new FollowException("[401] 请求参数有误");
        }

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        try {
            tx.begin(60000, "my_test_tx_group");
            LoggingFramework.info("开始关注商铺事务：" + tx.getXid());

            boolean result1 = followService.followShop(targetId, followerId);
            boolean result2 = shopService.incrementShopFollows(targetId);
            boolean result3 = userService.incrementFollowingShops(followerId);

            if (!result1 || !result2 || !result3) {
                LoggingFramework.warning("关注商铺失败，准备回滚事务");
                tx.rollback();
                throw new FollowException("[400] 关注商铺失败");
            }

            LoggingFramework.info("关注商铺成功，提交事务");
            tx.commit();

            Map<String, Object> responseData = Map.of("message", "关注商铺成功");
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("发生异常，强制回滚事务: " + e.getMessage());
            try {
                if (tx != null && RootContext.inGlobalTransaction()) {
                    tx.rollback();
                    LoggingFramework.info("关注商铺事务已手动回滚");
                }
            } catch (Exception rollbackEx) {
                LoggingFramework.severe("事务回滚失败: " + rollbackEx.getMessage());
            }

            throw new FollowException("[500] 关注商铺异常：" + e.getMessage());
        }
    }

    /**
     * 取消关注商铺
     */
    @RequestMapping(value = "/unfollowShop",methodType = RequestMethod.POST)
    public void unfollowShop(@MyRequestBody Follows follows, HttpServletResponse resp)throws IOException {
        Integer targetId = follows.getTargetId();
        Integer followerId = follows.getFollowerId();

        if (targetId == null || followerId == null) {
            throw new FollowException("[401] 请求参数有误");
        }

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        try {
            tx.begin(60000, "my_test_tx_group");
            LoggingFramework.info("开始取消关注商铺事务：" + tx.getXid());

            boolean result1 = followService.unfollowShop(targetId, followerId);
            boolean result2 = shopService.lowShopFollows(targetId);
            boolean result3 = userService.lowFollowingShops(followerId);

            if (!result1 || !result2 || !result3) {
                LoggingFramework.warning("取消关注商铺失败，准备回滚事务");
                tx.rollback();
                throw new FollowException("[400] 取消关注商铺失败");
            }

            LoggingFramework.info("取消关注商铺成功，提交事务");
            tx.commit();

            Map<String, Object> responseData = Map.of("message", "取消关注商铺成功");
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("发生异常，强制回滚事务: " + e.getMessage());
            try {
                if (tx != null && RootContext.inGlobalTransaction()) {
                    tx.rollback();
                    LoggingFramework.info("取消关注商铺事务已手动回滚");
                }
            } catch (Exception rollbackEx) {
                LoggingFramework.severe("事务回滚失败: " + rollbackEx.getMessage());
            }

            throw new FollowException("[500] 取消关注商铺异常：" + e.getMessage());
        }
    }

    /**
     * 查询关注的商铺列表
     */
    @RequestMapping(value = "/followingShops",methodType = RequestMethod.POST)
    public void followingShops(@MyRequestBody Follows follows, HttpServletResponse resp)throws IOException {
        Integer followerId = follows.getFollowerId();

        if(followerId==null){
            throw new FollowException("[401] 请求参数有误");
        }

        List<Integer> result = followService.followingShops(followerId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "关注的商铺列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 关注的商铺列表查询失败");
        }
    }

    /**
     * 查询关注的用户列表
     */
    @RequestMapping(value = "/followingUsers",methodType = RequestMethod.POST)
    public void followingUsers(@MyRequestBody Follows follows, HttpServletResponse resp)throws IOException {
        Integer followerId = follows.getFollowerId();

        if(followerId==null){
            throw new FollowException("[401] 请求参数有误");
        }

        List<Integer> result = followService.followingUsers(followerId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "关注的用户列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 关注的用户列表查询失败");
        }
    }

    /**
     * 查询商铺被关注的用户列表
     */
    @RequestMapping(value = "/shopFollowed",methodType = RequestMethod.POST)
    public void shopFollowed(@MyRequestBody Follows follows, HttpServletResponse resp)throws IOException {
        Integer targetId = follows.getTargetId();

        if(targetId==null){
            throw new FollowException("[401] 请求参数有误");
        }

        List<Integer> result = followService.shopFollowed(targetId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "商铺被关注的用户列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 商铺被关注的用户列表查询失败");
        }
    }

    /**
     * 查询用户被关注的用户列表
     */
    @RequestMapping(value = "/userFollowed",methodType = RequestMethod.POST)
    public void userFollowed(@MyRequestBody Follows follows, HttpServletResponse resp)throws IOException {
        Integer targetId = follows.getTargetId();

        if(targetId==null){
            throw new FollowException("[401] 请求参数有误");
        }

        List<Integer> result = followService.userFollowed(targetId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "用户被关注的用户列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 用户被关注的用户列表查询失败");
        }
    }
}
