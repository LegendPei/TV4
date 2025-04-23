package com.peitianbao.www.dao;


import com.peitianbao.www.model.po.UsersPO;

/**
 * @author leg
 */
public interface UserDao {
    /**
     * 插入用户
     * @param user 插入的用户
     * @return 是否插入成功
     */
    boolean insertUser(UsersPO user);

    /**
     * 更新用户信息
     * @param user 待更新的用户信息
     * @return 是否更新成功
     */
    boolean updateUser(UsersPO user);

    /**
     * 删除用户
     * @param userId 需要删除的用户id
     * @return 是否删除成功
     */
    boolean deleteUser(Integer userId);

    /**
     * 用户登录
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @return 用户登录的实体类
     */
    UsersPO loginUser(String userAccount, String userPassword);

    /**
     * 展示用户信息
     * @param userId 用户id
     * @return 用户信息
     */
    UsersPO showUserInfo(Integer userId);

    /**
     * 通过用户账户查询用户id
     * @param userAccount 用户账户
     * @return 用户id
     */
    Integer findUserIdByAccount(String userAccount);

    /**
     * 增加用户被关注数
     * @param userId 用户id
     * @return 是否增加成功
     */
    boolean incrementUserFollowers(Integer userId);

    /**
     * 减少用户被关注数
     * @param userId 用户id
     * @return 是否增加成功
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
     * @return 是否增加成功
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
     * @return 是否增加成功
     */
    boolean lowFollowingShops(Integer userId);
}
