package com.peitianbao.www.api;

/**
 * @author leg
 */
public interface UserService {
    /**
     * 增加用户被关注数
     * @param userId 用户id
     * @return 是否增加成功
     */
    boolean incrementUserFollowers(Integer userId);

    /**
     * 减少用户被关注数
     * @param userId 用户id
     * @return 是否减少成功
     */
    boolean lowUserFollowers(Integer userId);

    /**
     * 增加用户关注用户数
     * @param userId 用户id
     * @return 是否增加成功
     */
    boolean incrementFollowingUsers(Integer userId);

    /**
     * 减少用户关注用户数
     * @param userId 用户id
     * @return 是否减少成功
     */
    boolean lowFollowingUsers(Integer userId);

    /**
     * 增加用户关注商铺数
     * @param userId 用户id
     * @return 是否增加成功
     */
    boolean incrementFollowingShops(Integer userId);

    /**
     * 减少用户关注商铺数
     * @param userId 用户id
     * @return 是否减少成功
     */
    boolean lowFollowingShops(Integer userId);
}
