package com.peitianbao.www.dao;


import com.peitianbao.www.exception.FollowException;
import com.peitianbao.www.exception.LikeException;
import com.peitianbao.www.model.Follows;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Dao;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Dao
public class FollowDaoImpl implements FollowDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    public boolean followUser(Integer userId, Integer followerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", userId);
            params.put("followerId", followerId);

            Map<String, Object> param = new HashMap<>();
            param.put("userId", userId);

            int result = sqlSession.executeUpdate("FollowMapper.insertFollow", params);
            int result1 = sqlSession.executeUpdate("UserMapper.incrementUserFollows", param);
            return result!=0&&result1!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入关注失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new FollowException("插入关注失败", e);
        }
    }

    @Override
    public boolean followShop(Integer shopId, Integer followerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", shopId);
            params.put("followerId", followerId);

            Map<String, Object> param = new HashMap<>();
            param.put("shopId", shopId);

            int result = sqlSession.executeUpdate("FollowMapper.insertFollow", params);
            int result1 = sqlSession.executeUpdate("ShopMapper.incrementShopFollows", param);
            return result!=0&&result1!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入关注失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new FollowException("插入关注失败", e);
        }
    }

    @Override
    public boolean unfollowUser(Integer userId, Integer followerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", userId);
            params.put("followerId", followerId);

            Map<String, Object> param = new HashMap<>();
            param.put("userId", userId);

            int result = sqlSession.executeUpdate("FollowMapper.deleteFollow", params);
            int result1 = sqlSession.executeUpdate("UserMapper.lowUserFollows", param);
            return result!=0&&result1!=0;
        } catch (Exception e) {
            LoggingFramework.severe("取消关注失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new FollowException("取消关注失败", e);
        }
    }

    @Override
    public boolean unfollowShop(Integer shopId, Integer followerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", shopId);
            params.put("followerId", followerId);

            Map<String, Object> param = new HashMap<>();
            param.put("shopId", shopId);

            int result = sqlSession.executeUpdate("FollowMapper.deleteFollow", params);
            int result1 = sqlSession.executeUpdate("ShopMapper.lowShopFollows", param);
            return result!=0&&result1!=0;
        } catch (Exception e) {
            LoggingFramework.severe("取消关注失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new FollowException("取消关注失败", e);
        }
    }

    @Override
    public List<Follows> followingShops(Integer followerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("followerId", followerId);

            LoggingFramework.info("尝试查询用户 ID：" + followerId + " 的关注商铺记录");
            return sqlSession.executeQueryForList("FollowMapper.selectFollowingShops", params, Follows.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询关注商铺记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Follows> followingUsers(Integer followerId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("followerId", followerId);

            LoggingFramework.info("尝试查询用户 ID：" + followerId + " 的关注用户记录");
            return sqlSession.executeQueryForList("FollowMapper.selectFollowingUsers", params, Follows.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询关注用户记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }
}
