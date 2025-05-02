package com.peitianbao.www.api;

import java.util.List;

/**
 * @author leg
 */
public interface FollowService {
    /**
     * 查询关注的商铺列表
     * @param followerId 用户id
     * @return 商铺id集合
     */
    List<Integer> followingShops(Integer followerId);

    /**
     * 查询关注的用户列表
     * @param followerId 用户id
     * @return 用户id集合
     */
    List<Integer> followingUsers(Integer followerId);
}
