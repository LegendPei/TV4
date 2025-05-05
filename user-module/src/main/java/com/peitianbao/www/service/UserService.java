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

import java.util.Random;

/**
 * @author leg
 */
@Service
@DubboService
public class UserService implements com.peitianbao.www.api.UserService {

    @Autowired
    private UserDao userDao;

    private static final String USER_INFO_PREFIX = "user:info:";

    //基础缓存过期时间
    private static final int BASE_CACHE_EXPIRE_SECONDS = 3600;

    //随机化缓存过期时间的最大偏移量
    private static final int RANDOM_EXPIRE_OFFSET = 300;

    //缓存空值的过期时间
    private static final int EMPTY_CACHE_EXPIRE_SECONDS = 300;

    private final Gson gson = new Gson();

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

        Integer userId = userDao.findUserIdByAccount(userAccount);
        if (userId == null) {
            throw new UserException("登录失败：账号不存在");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);

        UsersPO userPo;
        if (cachedUserJson != null) {
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            userPo = gson.fromJson(cachedUserJson, UsersPO.class);
        } else {
            userPo = userDao.loginUser(userAccount, userPassword);
            if (userPo == null) {
                RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new UserException("登录失败：账号或密码错误");
            }
            RedisUtil.set(cacheKey, gson.toJson(userPo), getRandomExpireTime());
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
            String cacheKey = USER_INFO_PREFIX + userId;
            RedisUtil.set(cacheKey, gson.toJson(userPo), getRandomExpireTime());
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
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            userPo = gson.fromJson(cachedUserJson, UsersPO.class);
        } else {
            userPo = userDao.showUserInfo(userId);
            if (userPo == null) {
                RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new UserException("用户信息不存在");
            }
            RedisUtil.set(cacheKey, gson.toJson(userPo), getRandomExpireTime());
        }
        return new UsersDTO(userPo);
    }

    /**
     * 增加用户被关注数
     */
    @Override
    public boolean incrementUserFollowers(Integer userId) {
        boolean result = userDao.incrementUserFollowers(userId);
        if (!result) {
            throw new UserException("用户增加被关注失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            user = gson.fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowers();
        user.setFollowers(currentFollows + 1);
        RedisUtil.set(cacheKey, gson.toJson(user), getRandomExpireTime());

        return true;
    }

    /**
     * 减少用户被关注数
     */
    @Override
    public boolean lowUserFollowers(Integer userId) {
        boolean result = userDao.lowUserFollowers(userId);
        if (!result) {
            throw new UserException("用户减少被关注失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            user = gson.fromJson(cachedUserJson, UsersPO.class);
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
        RedisUtil.set(cacheKey, gson.toJson(user), getRandomExpireTime());

        return true;
    }

    /**
     * 增加用户关注用户数
     */
    @Override
    public boolean incrementFollowingUsers(Integer userId) {
        boolean result = userDao.incrementFollowingUsers(userId);
        if (!result) {
            throw new UserException("用户增加关注用户失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            user = gson.fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowingUsers();
        user.setFollowingUsers(currentFollows + 1);
        RedisUtil.set(cacheKey, gson.toJson(user), getRandomExpireTime());

        return true;
    }

    /**
     * 减少用户关注用户数
     */
    @Override
    public boolean lowFollowingUsers(Integer userId) {
        boolean result = userDao.lowFollowingUsers(userId);
        if (!result) {
            throw new UserException("用户减少关注用户失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            user = gson.fromJson(cachedUserJson, UsersPO.class);
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
        RedisUtil.set(cacheKey, gson.toJson(user), getRandomExpireTime());

        return true;
    }

    /**
     * 增加用户关注商铺数
     */
    @Override
    public boolean incrementFollowingShops(Integer userId) {
        boolean result = userDao.incrementFollowingShops(userId);
        if (!result) {
            throw new UserException("用户增加关注商铺失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            user = gson.fromJson(cachedUserJson, UsersPO.class);
        } else {
            user = userDao.showUserInfo(userId);
            if (user == null) {
                throw new UserException("用户信息不存在");
            }
        }

        int currentFollows = user.getFollowingShops();
        user.setFollowingShops(currentFollows + 1);
        RedisUtil.set(cacheKey, gson.toJson(user), getRandomExpireTime());

        return true;
    }

    /**
     * 减少用户关注商铺数
     */
    @Override
    public boolean lowFollowingShops(Integer userId) {
        boolean result = userDao.lowFollowingShops(userId);
        if (!result) {
            throw new UserException("用户减少关注商铺失败");
        }

        String cacheKey = USER_INFO_PREFIX + userId;
        String cachedUserJson = RedisUtil.get(cacheKey);
        UsersPO user;
        if (cachedUserJson != null) {
            if ("NOT_EXISTS".equals(cachedUserJson)) {
                throw new UserException("用户信息不存在");
            }
            user = gson.fromJson(cachedUserJson, UsersPO.class);
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
        RedisUtil.set(cacheKey, gson.toJson(user), getRandomExpireTime());

        return true;
    }

    /**
     * 获取随机化的缓存过期时间
     */
    private int getRandomExpireTime() {
        return BASE_CACHE_EXPIRE_SECONDS + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
    }
}
