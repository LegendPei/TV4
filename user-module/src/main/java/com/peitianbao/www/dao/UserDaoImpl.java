package com.peitianbao.www.dao;


import com.peitianbao.www.exception.UserException;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Dao;
import com.peitianbao.www.model.po.UsersPO;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leg
 */
@Dao
public class UserDaoImpl implements UserDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    @LoggingFramework.Log(message = "用户注册操作")
    public boolean insertUser(UsersPO user) {
        try {
            //对密码进行加密
            String hashedPassword = BCrypt.hashpw(user.getUserPassword(), BCrypt.gensalt());

            Map<String, Object> params = new HashMap<>();
            params.put("userName", user.getUserName());
            params.put("userAccount", user.getUserAccount());
            params.put("userPassword", hashedPassword);

            LoggingFramework.info("尝试插入用户：" + user.getUserName());

            int result = sqlSession.executeUpdate("UserMapper.insertUser", params);

            //记录成功日志
            LoggingFramework.info("用户插入成功：" + user.getUserName());
            return result != 0;
        } catch (Exception e) {
            //记录错误日志并抛出运行时异常
            LoggingFramework.severe("插入用户失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new UserException("插入用户失败", e);
        }
    }

    @Override
    public boolean updateUser(UsersPO user) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", user.getUserId());

            if (user.getUserName() != null && !user.getUserName().isEmpty()) {
                params.put("userName", user.getUserName());
            }
            if (user.getUserAccount() != null && !user.getUserAccount().isEmpty()) {
                params.put("userAccount", user.getUserAccount());
            }
            if (user.getUserPassword() != null && !user.getUserPassword().isEmpty()) {
                String hashedPassword = BCrypt.hashpw(user.getUserPassword(), BCrypt.gensalt());
                params.put("userPassword", hashedPassword);
            }

            LoggingFramework.info("尝试更新用户 ID：" + user.getUserId());

            int result = sqlSession.executeUpdate("UserMapper.updateUser", params);

            //记录成功日志
            LoggingFramework.info("用户更新成功：" + user.getUserName());
            return result!=0;
        } catch (Exception e) {
            //记录错误日志并抛出运行时异常
            LoggingFramework.severe("更新用户失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new UserException("更新用户失败", e);
        }
    }

    @Override
    @LoggingFramework.Log(message = "删除用户操作")
    public boolean deleteUser(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试删除用户 ID：" + userId);

            int result = sqlSession.executeUpdate("UserMapper.deleteUserById", params);

            //记录成功日志
            LoggingFramework.info("用户删除成功：" + userId);
            return result!=0;
        } catch (Exception e) {
            //记录错误日志并抛出运行时异常
            LoggingFramework.severe("删除用户失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new UserException("删除用户失败", e);
        }
    }

    @Override
    public UsersPO loginUser(String userAccount, String userPassword) {
        try {
            //准备参数
            Map<String, Object> params = new HashMap<>();
            params.put("account", userAccount);

            //记录日志：尝试查询用户
            LoggingFramework.info("尝试查询用户，账号：" + userAccount);

            //执行查询
            UsersPO user = sqlSession.executeQueryForObject("UserMapper.selectUserByAccountAndPassword",params,UsersPO.class);

            //检查查询结果
            if (user == null) {
                //如果未找到用户,记录日志并返回null
                LoggingFramework.warning("未找到用户，账号：" + userAccount);
                return null;
            }

            //获取数据库中的加密密码
            String hashedPassword = user.getUserPassword();

            //验证密码是否匹配
            if (BCrypt.checkpw(userPassword, hashedPassword)) {
                LoggingFramework.info("查询到用户：" + user.getUserName());
                return user;
            } else {
                LoggingFramework.warning("用户密码错误，账号：" + userAccount);
                return null;
            }
        } catch (Exception e) {
            LoggingFramework.severe("查询用户失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public UsersPO showUserInfo(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试查询用户 ID：" + userId);

            UsersPO user = sqlSession.executeQueryForObject("UserMapper.selectUserById", params, UsersPO.class);

            if (user != null) {
                //记录成功日志
                LoggingFramework.info("查询到用户：" + user.getUserName());
            } else {
                //记录未找到用户的日志
                LoggingFramework.warning("未找到用户 ID：" + userId);
            }
            return user;
        } catch (Exception e) {
            //记录错误日志
            LoggingFramework.severe("查询用户失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public Integer findUserIdByAccount(String userAccount) {
        try {
            //准备参数
            Map<String, Object> params = new HashMap<>();
            params.put("account", userAccount);

            //执行查询
            UsersPO user = sqlSession.executeQueryForObject("UserMapper.selectUserByAccount",params,UsersPO.class);

            return user.getUserId();
        } catch (Exception e) {
            LoggingFramework.severe("搜索用户失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public boolean incrementUserFollowers(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试更新用户被关注数：userId = " + userId);
            int result = sqlSession.executeUpdate("UserMapper.incrementUserFollowers", params);
            if (result > 0) {
                LoggingFramework.info("用户被关注更新成功：userId = " + userId);
            } else {
                LoggingFramework.warning("用户被关注更新失败：userId = " + userId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("用户被关注失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new RuntimeException("用户被关注数失败", e);
        }
    }

    @Override
    public boolean lowUserFollowers(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试更新用户被关注数：userId = " + userId);
            int result = sqlSession.executeUpdate("UserMapper.lowUserFollowers", params);
            if (result > 0) {
                LoggingFramework.info("用户被关注更新成功：userId = " + userId);
            } else {
                LoggingFramework.warning("用户被关注更新失败：userId = " + userId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("用户被关注失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new RuntimeException("用户被关注数失败", e);
        }
    }

    @Override
    public boolean incrementFollowingUsers(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试更新用户关注用户数：userId = " + userId);
            int result = sqlSession.executeUpdate("UserMapper.incrementFollowingUsers", params);
            if (result > 0) {
                LoggingFramework.info("用户关注用户更新成功：userId = " + userId);
            } else {
                LoggingFramework.warning("用户关注用户更新失败：userId = " + userId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("用户关注用户数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new RuntimeException("用户关注用户数失败", e);
        }
    }

    @Override
    public boolean lowFollowingUsers(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试更新用户关注用户数：userId = " + userId);
            int result = sqlSession.executeUpdate("UserMapper.lowFollowingUsers", params);
            if (result > 0) {
                LoggingFramework.info("用户关注用户更新成功：userId = " + userId);
            } else {
                LoggingFramework.warning("用户关注用户更新失败：userId = " + userId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("用户关注用户数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new RuntimeException("用户关注用户数失败", e);
        }
    }

    @Override
    public boolean incrementFollowingShops(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试更新用户关注商铺数：userId = " + userId);
            int result = sqlSession.executeUpdate("UserMapper.incrementFollowingShops", params);
            if (result > 0) {
                LoggingFramework.info("用户关注商铺更新成功：userId = " + userId);
            } else {
                LoggingFramework.warning("用户关注商铺更新失败：userId = " + userId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("用户关注商铺数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new RuntimeException("用户关注商铺数失败", e);
        }
    }

    @Override
    public boolean lowFollowingShops(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            LoggingFramework.info("尝试更新用户关注商铺数：userId = " + userId);
            int result = sqlSession.executeUpdate("UserMapper.lowFollowingShops", params);
            if (result > 0) {
                LoggingFramework.info("用户关注商铺更新成功：userId = " + userId);
            } else {
                LoggingFramework.warning("用户关注商铺更新失败：userId = " + userId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("用户关注商铺数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new RuntimeException("用户关注商铺数失败", e);
        }
    }
}
