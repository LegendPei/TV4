package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.peitianbao.www.dao.UserDao;
import com.peitianbao.www.exception.UserException;
import com.peitianbao.www.model.dto.UsersDTO;
import com.peitianbao.www.model.po.UsersPO;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.token.RedisUtil;

/**
 * @author leg
 */
@Service
@DubboService
public class UserService implements com.peitianbao.www.api.UserService {

    @Autowired
    private UserDao userDao;

    //用户信息缓存前缀
    private static final String USER_INFO_PREFIX = "user:info:";

    //缓存过期时间（单位：秒）
    private static final int CACHE_EXPIRE_SECONDS = 3600;

    /**
     * 用户注册
     */
    public boolean userRegister(String userName, String userPassword, String userAccount) {
        UsersPO userPo = new UsersPO();
        userPo.setUserName(userName);
        userPo.setUserAccount(userAccount);
        userPo.setUserPassword(userPassword);

        boolean result = userDao.insertUser(userPo);
        if (result) {
            return true;
        } else {
            throw new UserException("用户注册失败:已注册的账号或名字");
        }
    }

    /**
     * 用户登录
     */
    public UsersDTO userLogin(String userAccount, String userPassword) {
        if (userAccount == null || userPassword == null || userPassword.isEmpty() || userAccount.isEmpty()) {
            throw new UserException("登录失败：存在输入的信息为空");
        }

        //查询用户ID
        Integer userId = userDao.findUserIdByAccount(userAccount);
        if (userId == null) {
            throw new UserException("登录失败：账号不存在");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);

        UsersPO userPo;
        if (cachedUserJson != null) {
            //从缓存中获取用户信息
            userPo = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            //从数据库中加载用户信息
            userPo = userDao.loginUser(userAccount, userPassword);
            if (userPo != null) {
                //写入缓存
                RedisUtil.set(cacheKey, new Gson().toJson(userPo), CACHE_EXPIRE_SECONDS);
            }
        }

        if (userPo == null || !userPo.getUserPassword().equals(userPassword)) {
            throw new UserException("登录失败：账号或密码错误");
        }

        return new UsersDTO(userPo);
    }

    /**
     * 用户更新
     */
    public boolean userUpdate(Integer userId, String userName, String userPassword, String userAccount) {
        UsersPO userPo = new UsersPO();
        userPo.setUserId(userId);
        userPo.setUserName(userName);
        userPo.setUserAccount(userAccount);
        userPo.setUserPassword(userPassword);

        boolean result = userDao.updateUser(userPo);
        if (result) {
            //更新缓存
            String cacheKey = USER_INFO_PREFIX + userId;
            RedisUtil.set(cacheKey, new Gson().toJson(userPo), CACHE_EXPIRE_SECONDS);
            return true;
        } else {
            throw new UserException("用户更新失败:已有的账号或名字");
        }
    }

    /**
     * 用户注销
     */
    public boolean userDelete(Integer userId) {
        boolean result = userDao.deleteUser(userId);
        if (result) {
            //清除缓存
            String cacheKey = USER_INFO_PREFIX + userId;
            RedisUtil.delete(cacheKey);
            return true;
        } else {
            throw new UserException("用户注销失败");
        }
    }

    /**
     * 查询用户信息
     */
    public UsersDTO showUserInfo(Integer userId) {
        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);

        UsersPO userPo;
        if (cachedUserJson != null) {
            //从缓存中获取用户信息
            userPo = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            //从数据库中加载用户信息
            userPo = userDao.showUserInfo(userId);
            if (userPo != null) {
                //写入缓存
                RedisUtil.set(cacheKey, new Gson().toJson(userPo), CACHE_EXPIRE_SECONDS);
            }
        }

        if (userPo == null) {
            throw new UserException("用户信息不存在");
        }

        return new UsersDTO(userPo);
    }

    /**
     * 增加用户被关注数
     */
    @Override
    public boolean incrementUserFollowers(Integer userId) {
        //更新数据库中的关注数
        boolean result = userDao.incrementUserFollowers(userId);
        if (!result) {
            throw new UserException("用户增加被关注失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;

        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            user = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowers();
        user.setFollowers(currentFollows + 1);
        RedisUtil.set(cacheKey, new Gson().toJson(user), CACHE_EXPIRE_SECONDS);

        return true;
    }

    /**
     * 减少用户被关注数
     */
    @Override
    public boolean lowUserFollowers(Integer userId) {
        //更新数据库中的关注数
        boolean result = userDao.lowUserFollowers(userId);
        if (!result) {
            throw new UserException("用户减少被关注失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;

        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            user = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowers();
        if (currentFollows <= 0) {
            throw new UserException("关注数不能为负数");
        }
        user.setFollowers(currentFollows - 1);
        RedisUtil.set(cacheKey, new Gson().toJson(user), CACHE_EXPIRE_SECONDS);

        return true;
    }

    /**
     * 增加用户关注用户数
     */
    @Override
    public boolean incrementFollowingUsers(Integer userId) {
        //更新数据库中的关注数
        boolean result = userDao.incrementFollowingUsers(userId);
        if (!result) {
            throw new UserException("用户增加关注用户失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;

        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            user = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowingUsers();
        user.setFollowingUsers(currentFollows + 1);
        RedisUtil.set(cacheKey, new Gson().toJson(user), CACHE_EXPIRE_SECONDS);

        return true;
    }

    /**
     * 减少用户关注用户数
     */
    @Override
    public boolean lowFollowingUsers(Integer userId) {
        //更新数据库中的关注数
        boolean result = userDao.lowFollowingUsers(userId);
        if (!result) {
            throw new UserException("用户减少关注用户失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;

        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            user = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowingUsers();
        if (currentFollows <= 0) {
            throw new UserException("关注数不能为负数");
        }
        user.setFollowingUsers(currentFollows - 1);
        RedisUtil.set(cacheKey, new Gson().toJson(user), CACHE_EXPIRE_SECONDS);

        return true;
    }

    /**
     * 增加用户关注商铺数
     */
    @Override
    public boolean incrementFollowingShops(Integer userId) {
        //更新数据库中的关注数
        boolean result = userDao.incrementFollowingShops(userId);
        if (!result) {
            throw new UserException("用户增加关注商铺失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;

        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            user = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowingShops();
        user.setFollowingShops(currentFollows + 1);
        RedisUtil.set(cacheKey, new Gson().toJson(user), CACHE_EXPIRE_SECONDS);

        return true;
    }

    /**
     * 减少用户关注商铺数
     */
    @Override
    public boolean lowFollowingShops(Integer userId) {
        //更新数据库中的关注数
        boolean result = userDao.lowFollowingShops(userId);
        if (!result) {
            throw new UserException("用户减少关注商铺失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;

        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            user = new Gson().fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowingShops();
        if (currentFollows <= 0) {
            throw new UserException("关注数不能为负数");
        }
        user.setFollowingShops(currentFollows - 1);
        RedisUtil.set(cacheKey, new Gson().toJson(user), CACHE_EXPIRE_SECONDS);

        return true;
    }
}
