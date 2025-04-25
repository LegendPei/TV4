package com.peitianbao.www.dao;

import com.peitianbao.www.model.Follows;

import java.util.List;

/**
 * @author leg
 */
public interface FollowDao {
    /**
     * 关注操作
     * @param userId 关注用户id
     * @param followerId 关注者id
     * @return 是否关注成功
     */
    boolean followUser(Integer userId, Integer followerId);

    /**
     * 关注操作
     * @param shopId 关注商铺id
     * @param followerId 关注者id
     * @return 是否关注成功
     */
    boolean followShop(Integer shopId, Integer followerId);

    /**
     * 取消关注
     * @param userId 要取消关注用户id
     * @param followerId 关注者id
     * @return 是否取消关注成功
     */
    boolean unfollowUser(Integer userId, Integer followerId);

    /**
     * 取消关注
     * @param shopId 要取消关注商铺id
     * @param followerId 关注者id
     * @return 是否取消关注成功
     */
    boolean unfollowShop(Integer shopId, Integer followerId);

    /**
     * 查询用户关注的商铺id
     * @param followerId 用户id
     * @return 商铺id的集合
     */
    List<Follows> followingShops(Integer followerId);

    /**
     * 查询用户关注的用户id
     * @param followerId 用户id
     * @return 关注的用户id的集合
     */
    List<Follows> followingUsers(Integer followerId);

    /**
     * 查询商铺被那些用户关注了
     * @param shopId 商铺id
     * @return 用户id的集合
     */
    List<Follows> shopFollowed(Integer shopId);

    /**
     * 查询用户被那些用户关注了
     * @param userId 用户id
     * @return 用户id的集合
     */
    List<Follows> userFollowed(Integer userId);
}
