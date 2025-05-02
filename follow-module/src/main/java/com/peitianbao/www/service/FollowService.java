package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.dao.FollowDao;
import com.peitianbao.www.exception.FollowException;
import com.peitianbao.www.model.Follows;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author leg
 */
@Service
@DubboService
public class FollowService implements com.peitianbao.www.api.FollowService {

    @Autowired
    private FollowDao followDao;

    private static final String FOLLOW_SHOPS_PREFIX = "follow:shops:";
    private static final String FOLLOW_USERS_PREFIX = "follow:users:";
    private static final String SHOP_FOLLOWED_PREFIX = "follow:shop-followed:";
    private static final String USER_FOLLOWED_PREFIX = "follow:user-followed:";

    //基础缓存过期时间
    private static final int BASE_CACHE_EXPIRE_SECONDS = 3600;

    //随机化缓存过期时间的最大偏移量
    private static final int RANDOM_EXPIRE_OFFSET = 300;

    //缓存空值的过期时间
    private static final int EMPTY_CACHE_EXPIRE_SECONDS = 300;

    private final Gson gson = new Gson();

    /**
     * 关注用户
     */
    public boolean followUser(Integer userId, Integer followerId) {
        boolean result = followDao.followUser(userId, followerId);
        if (result) {
            RedisUtil.delete(FOLLOW_USERS_PREFIX + followerId);
            RedisUtil.delete(USER_FOLLOWED_PREFIX + userId);
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
            RedisUtil.delete(FOLLOW_SHOPS_PREFIX + followerId);
            RedisUtil.delete(SHOP_FOLLOWED_PREFIX + shopId);
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
            RedisUtil.delete(FOLLOW_USERS_PREFIX + followerId);
            RedisUtil.delete(USER_FOLLOWED_PREFIX + userId);
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
            RedisUtil.delete(FOLLOW_SHOPS_PREFIX + followerId);
            RedisUtil.delete(SHOP_FOLLOWED_PREFIX + shopId);
            return true;
        } else {
            throw new FollowException("取消关注商铺失败");
        }
    }

    /**
     * 查询关注的商铺列表
     */
    @Override
    public List<Integer> followingShops(Integer followerId) {
        String cacheKey = FOLLOW_SHOPS_PREFIX + followerId;
        String cachedShopsJson = RedisUtil.get(cacheKey);

        List<Integer> followingShops;
        if (cachedShopsJson != null) {
            followingShops = gson.fromJson(cachedShopsJson, new TypeToken<List<Integer>>() {}.getType());
            if ("[]".equals(cachedShopsJson)) {
                throw new FollowException("关注的商铺列表为空");
            }
        } else {
            List<Follows> followsList = followDao.followingShops(followerId);
            if (followsList == null || followsList.isEmpty()) {
                RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new FollowException("关注的商铺列表为空");
            }

            followingShops = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());

            RedisUtil.set(cacheKey, gson.toJson(followingShops), getRandomExpireTime());
        }
        return followingShops;
    }

    /**
     * 查询关注的用户列表
     */
    @Override
    public List<Integer> followingUsers(Integer followerId) {
        String cacheKey = FOLLOW_USERS_PREFIX + followerId;
        String cachedUsersJson = RedisUtil.get(cacheKey);

        List<Integer> followingUsers;
        if (cachedUsersJson != null) {
            followingUsers = gson.fromJson(cachedUsersJson, new TypeToken<List<Integer>>() {}.getType());
            if ("[]".equals(cachedUsersJson)) {
                throw new FollowException("关注的用户列表为空");
            }
        } else {
            List<Follows> followsList = followDao.followingUsers(followerId);
            if (followsList == null || followsList.isEmpty()) {
                RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new FollowException("关注的用户列表为空");
            }

            followingUsers = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());

            RedisUtil.set(cacheKey, gson.toJson(followingUsers), getRandomExpireTime());
        }
        return followingUsers;
    }

    /**
     * 查询商铺被关注的用户列表
     */
    public List<Integer> shopFollowed(Integer shopId) {
        String cacheKey = SHOP_FOLLOWED_PREFIX + shopId;
        String cachedUsersJson = RedisUtil.get(cacheKey);

        List<Integer> followedUsers;
        if (cachedUsersJson != null) {
            followedUsers = gson.fromJson(cachedUsersJson, new TypeToken<List<Integer>>() {}.getType());
            if ("[]".equals(cachedUsersJson)) {
                throw new FollowException("商铺被关注的用户列表为空");
            }
        } else {
            List<Follows> followsList = followDao.shopFollowed(shopId);
            if (followsList == null || followsList.isEmpty()) {
                RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new FollowException("商铺被关注的用户列表为空");
            }

            followedUsers = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());

            RedisUtil.set(cacheKey, gson.toJson(followedUsers), getRandomExpireTime());
        }

        return followedUsers;
    }

    /**
     * 查询用户被关注的用户列表
     */
    public List<Integer> userFollowed(Integer userId) {
        String cacheKey = USER_FOLLOWED_PREFIX + userId;
        String cachedUsersJson = RedisUtil.get(cacheKey);

        List<Integer> followedUsers;
        if (cachedUsersJson != null) {
            followedUsers = gson.fromJson(cachedUsersJson, new TypeToken<List<Integer>>() {}.getType());
            if ("[]".equals(cachedUsersJson)) {
                throw new FollowException("用户被关注的用户列表为空");
            }
        } else {
            List<Follows> followsList = followDao.userFollowed(userId);
            if (followsList == null || followsList.isEmpty()) {
                RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new FollowException("用户被关注的用户列表为空");
            }

            followedUsers = followsList.stream()
                    .map(Follows::getTargetId)
                    .collect(Collectors.toList());


            RedisUtil.set(cacheKey, gson.toJson(followedUsers), getRandomExpireTime());
        }
        return followedUsers;
    }

    /**
     * 获取随机化的缓存过期时间
     */
    private int getRandomExpireTime() {
        return BASE_CACHE_EXPIRE_SECONDS + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
    }
}