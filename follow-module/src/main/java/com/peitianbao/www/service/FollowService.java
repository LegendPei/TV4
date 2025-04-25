package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.dao.FollowDao;
import com.peitianbao.www.exception.FollowException;
import com.peitianbao.www.model.Follows;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author leg
 */
@Service
public class FollowService {

    @Autowired
    private FollowDao followDao;

    //缓存前缀
    private static final String FOLLOW_SHOPS_PREFIX = "follow:shops:";
    private static final String FOLLOW_USERS_PREFIX = "follow:users:";
    private static final String SHOP_FOLLOWED_PREFIX = "follow:shop-followed:";
    private static final String USER_FOLLOWED_PREFIX = "follow:user-followed:";

    //缓存过期时间（单位：秒）
    private static final int CACHE_EXPIRE_SECONDS = 3600;

    /**
     * 关注用户
     */
    public boolean followUser(Integer userId, Integer followerId) {
        boolean result = followDao.followUser(userId, followerId);
        if (result) {
            //清除缓存
            RedisUtil.delete(FOLLOW_SHOPS_PREFIX + followerId);
            RedisUtil.delete(FOLLOW_USERS_PREFIX + followerId);
            return true;
        } else {
            throw new FollowException("关注用户失败");
        }
    }

    /**
     * 关注商铺
     */
    public boolean followShop(Integer shopId, Integer followerId) {
        boolean result = followDao.followShop(shopId, followerId);
        if (result) {
            //清除缓存
            RedisUtil.delete(FOLLOW_SHOPS_PREFIX + followerId);
            RedisUtil.delete(FOLLOW_USERS_PREFIX + followerId);
            return true;
        } else {
            throw new FollowException("关注商铺失败");
        }
    }

    /**
     * 取消关注用户
     */
    public boolean unfollowUser(Integer userId, Integer followerId) {
        boolean result = followDao.unfollowUser(userId, followerId);
        if (result) {
            //清除缓存
            RedisUtil.delete(FOLLOW_SHOPS_PREFIX + followerId);
            RedisUtil.delete(FOLLOW_USERS_PREFIX + followerId);
            return true;
        } else {
            throw new FollowException("取消关注用户失败");
        }
    }

    /**
     * 取消关注商铺
     */
    public boolean unfollowShop(Integer shopId, Integer followerId) {
        boolean result = followDao.unfollowShop(shopId, followerId);
        if (result) {
            //清除缓存
            RedisUtil.delete(FOLLOW_SHOPS_PREFIX + followerId);
            RedisUtil.delete(FOLLOW_USERS_PREFIX + followerId);
            return true;
        } else {
            throw new FollowException("取消关注商铺失败");
        }
    }

    /**
     * 查询关注的商铺列表
     */
    public List<Integer> followingShops(Integer followerId) {
        String cacheKey = FOLLOW_SHOPS_PREFIX + followerId;
        String cachedShopsJson = RedisUtil.get(cacheKey);

        List<Integer> followingShops;
        if (cachedShopsJson != null) {
            //从缓存中获取关注的商铺列表
            followingShops = new Gson().fromJson(cachedShopsJson, new TypeToken<List<Integer>>() {}.getType());
        } else {
            //从数据库中加载关注的商铺列表
            List<Follows> followsList = followDao.followingShops(followerId);
            if (followsList == null || followsList.isEmpty()) {
                throw new FollowException("关注的商铺列表为空");
            }

            //提取targetId列表
            followingShops = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());

            //写入缓存
            RedisUtil.set(cacheKey, new Gson().toJson(followingShops), CACHE_EXPIRE_SECONDS);
        }

        return followingShops;
    }

    /**
     * 查询关注的用户列表
     */
    public List<Integer> followingUsers(Integer followerId) {
        String cacheKey = FOLLOW_USERS_PREFIX + followerId;
        String cachedUsersJson = RedisUtil.get(cacheKey);

        List<Integer> followingUsers;
        if (cachedUsersJson != null) {
            //从缓存中获取关注的用户列表
            followingUsers = new Gson().fromJson(cachedUsersJson, new TypeToken<List<Integer>>() {}.getType());
        } else {
            //从数据库中加载关注的用户列表
            List<Follows> followsList = followDao.followingUsers(followerId);
            if (followsList == null || followsList.isEmpty()) {
                throw new FollowException("关注的用户列表为空");
            }

            //提取targetId列表
            followingUsers = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());

            //写入缓存
            RedisUtil.set(cacheKey, new Gson().toJson(followingUsers), CACHE_EXPIRE_SECONDS);
        }

        return followingUsers;
    }

    /**
     * 查询商铺被关注的用户列表
     */
    public List<Integer> shopFollowed(Integer shopId) {
        String cacheKey = SHOP_FOLLOWED_PREFIX + shopId;
        String cachedUsersJson = RedisUtil.get(cacheKey);

        List<Integer> followingUsers;
        if (cachedUsersJson != null) {
            //从缓存中获取被关注的用户列表
            followingUsers = new Gson().fromJson(cachedUsersJson, new TypeToken<List<Integer>>() {}.getType());
        } else {
            //从数据库中加载被关注的用户列表
            List<Follows> followsList = followDao.shopFollowed(shopId);
            if (followsList == null || followsList.isEmpty()) {
                throw new FollowException("商铺被关注的用户列表为空");
            }

            followingUsers = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());

            RedisUtil.set(cacheKey, new Gson().toJson(followingUsers), CACHE_EXPIRE_SECONDS);
        }

        return followingUsers;
    }

    /**
     * 查询用户被关注的用户列表
     */
    public List<Integer> userFollowed(Integer userId) {
        String cacheKey = USER_FOLLOWED_PREFIX + userId;
        String cachedUsersJson = RedisUtil.get(cacheKey);

        List<Integer> followingUsers;
        if (cachedUsersJson != null) {
            //从缓存中获取被关注的用户列表
            followingUsers = new Gson().fromJson(cachedUsersJson, new TypeToken<List<Integer>>() {}.getType());
        } else {
            //从数据库中加载被关注的用户列表
            List<Follows> followsList = followDao.userFollowed(userId);
            if (followsList == null || followsList.isEmpty()) {
                throw new FollowException("用户被关注的用户列表为空");
            }

            followingUsers = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());

            RedisUtil.set(cacheKey, new Gson().toJson(followingUsers), CACHE_EXPIRE_SECONDS);
        }

        return followingUsers;
    }
}