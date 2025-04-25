package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.peitianbao.www.dao.ShopDao;
import com.peitianbao.www.exception.ShopException;
import com.peitianbao.www.model.dto.ShopsDTO;
import com.peitianbao.www.model.po.ShopsPO;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author leg
 */
@Service
@DubboService
public class ShopService implements com.peitianbao.www.api.ShopService {
    @Autowired
    private ShopDao shopDao;

    //商铺信息缓存前缀
    private static final String SHOP_INFO_PREFIX = "shop:info:";

    //缓存过期时间（单位：秒）
    private static final int CACHE_EXPIRE_SECONDS = 3600;

    /**
     * 商铺注册
     */
    public boolean shopRegister(String shopName, String shopAccount, String shopPassword, String shopAddress, String shopInfo) {
        ShopsPO shop = new ShopsPO();
        shop.setShopName(shopName);
        shop.setShopAccount(shopAccount);
        shop.setShopPassword(shopPassword);
        shop.setShopAddress(shopAddress);
        shop.setShopInfo(shopInfo);

        boolean result = shopDao.insertShop(shop);
        if (result) {
            return true;
        } else {
            throw new ShopException("商铺注册失败:已注册的账号或名字");
        }
    }

    /**
     * 商铺登录
     */
    public ShopsDTO shopLogin(String shopAccount, String shopPassword) {
        if (shopAccount == null || shopPassword == null || shopAccount.isEmpty() || shopPassword.isEmpty()) {
            throw new ShopException("登录失败：存在输入的信息为空");
        }

        //查询商铺ID
        Integer shopId = shopDao.findShopIdByAccount(shopAccount);
        if (shopId == null) {
            throw new ShopException("登录失败：账号不存在");
        }

        String cacheKey = SHOP_INFO_PREFIX + shopId;
        String cachedShopJson = RedisUtil.get(cacheKey);

        ShopsPO shop;
        if (cachedShopJson != null) {
            //从缓存中获取商铺信息
            shop = new Gson().fromJson(cachedShopJson, ShopsPO.class);
        } else {
            //从数据库中加载商铺信息
            shop = shopDao.loginShop(shopAccount, shopPassword);
            if (shop != null) {
                //写入缓存
                RedisUtil.set(cacheKey, new Gson().toJson(shop), CACHE_EXPIRE_SECONDS);
            }
        }

        if (shop == null || !shop.getShopPassword().equals(shopPassword)) {
            throw new ShopException("登录失败：账号或密码错误");
        }

        return new ShopsDTO(shop);
    }

    /**
     * 商铺更新
     */
    public boolean shopUpdate(Integer shopId, String shopName, String shopAccount, String shopPassword, String shopAddress, String shopInfo) {
        ShopsPO shop = new ShopsPO();
        shop.setShopId(shopId);
        shop.setShopName(shopName);
        shop.setShopAccount(shopAccount);
        shop.setShopPassword(shopPassword);
        shop.setShopAddress(shopAddress);
        shop.setShopInfo(shopInfo);

        boolean result = shopDao.updateShop(shop);
        if (result) {
            //更新缓存
            String cacheKey = SHOP_INFO_PREFIX + shopId;
            RedisUtil.set(cacheKey, new Gson().toJson(shop), CACHE_EXPIRE_SECONDS);
            return true;
        } else {
            throw new ShopException("商铺更新失败:已有的账号或名字");
        }
    }

    /**
     * 商铺注销
     */
    public boolean shopDelete(Integer shopId) {
        boolean result = shopDao.deleteShop(shopId);
        if (result) {
            //清除缓存
            String cacheKey = SHOP_INFO_PREFIX + shopId;
            RedisUtil.delete(cacheKey);
            return true;
        } else {
            throw new ShopException("商铺注销失败");
        }
    }

    /**
     * 查询商铺信息
     */
    public ShopsDTO showShopInfo(Integer shopId) {
        String cacheKey = SHOP_INFO_PREFIX + shopId;
        String cachedShopJson = RedisUtil.get(cacheKey);

        ShopsPO shop;
        if (cachedShopJson != null) {
            //从缓存中获取商铺信息
            shop = new Gson().fromJson(cachedShopJson, ShopsPO.class);
        } else {
            //从数据库中加载商铺信息
            shop = shopDao.showShopInfo(shopId);
            if (shop != null) {
                //写入缓存
                RedisUtil.set(cacheKey, new Gson().toJson(shop), CACHE_EXPIRE_SECONDS);
            }
        }

        if (shop == null) {
            throw new ShopException("商铺信息不存在");
        }

        return new ShopsDTO(shop);
    }

    /**
     * 查询所有商铺，支持排序
     */
    public List<ShopsDTO> findAllShops(String sortType) {
        List<ShopsPO> allShops = shopDao.findAllShops();

        //根据sortType进行排序
        Comparator<ShopsPO> comparator = switch (sortType == null ? "" : sortType.toLowerCase()) {
            case "likes" -> Comparator.comparing(ShopsPO::getShopLikes).reversed();
            case "followers" -> Comparator.comparing(ShopsPO::getShopFollowers).reversed();
            default -> Comparator.comparing(ShopsPO::getShopId);
        };

        //对商铺列表进行排序
        List<ShopsPO> sortedShops = allShops.stream()
                .sorted(comparator)
                .toList();

        //转换为DTO列表
        return sortedShops.stream()
                .map(ShopsDTO::new)
                .collect(Collectors.toList());
    }

    /**
     * 商铺点赞数加一
     */
    @Override
    public boolean incrementShopLikes(Integer shopId) {
        //更新数据库中的点赞数
        if (shopDao == null) {
            throw new IllegalStateException("ShopDao is not initialized!");
        }

        if (shopId == null) {
            throw new IllegalArgumentException("shopId cannot be null");
        }
        boolean result = shopDao.incrementShopLikes(shopId);
        if (!result) {
            throw new ShopException("商铺点赞失败");
        }

        String cacheKey = SHOP_INFO_PREFIX + shopId;

        String cachedShopJson = RedisUtil.get(cacheKey);
        ShopsPO shop;
        if (cachedShopJson != null) {
            shop = new Gson().fromJson(cachedShopJson, ShopsPO.class);

        } else {
            shop = shopDao.showShopInfo(shopId);
            if (shop == null) {
                throw new ShopException("商铺信息不存在");
            }
        }

        shop.setShopLikes(shop.getShopLikes() + 1);
        RedisUtil.set(cacheKey, new Gson().toJson(shop), CACHE_EXPIRE_SECONDS);
        return true;
    }

    /**
     * 商铺点赞数减一
     */
    @Override
    public boolean lowShopLikes(Integer shopId) {
        //更新数据库中的点赞数
        boolean result = shopDao.lowShopLikes(shopId);
        if (!result) {
            throw new ShopException("商铺减少点赞失败");
        }

        String cacheKey = SHOP_INFO_PREFIX + shopId;

        String cachedShopJson = RedisUtil.get(cacheKey);
        ShopsPO shop;
        if (cachedShopJson != null) {
            shop = new Gson().fromJson(cachedShopJson, ShopsPO.class);
        } else {
            shop = shopDao.showShopInfo(shopId);
            if (shop == null) {
                throw new ShopException("商铺信息不存在");
            }
        }

        int currentLikes = shop.getShopLikes();
        if (currentLikes <= 0) {
            throw new ShopException("点赞数不能为负数");
        }
        shop.setShopLikes(currentLikes - 1);
        RedisUtil.set(cacheKey, new Gson().toJson(shop), CACHE_EXPIRE_SECONDS);

        return true;
    }

    /**
     * 商铺关注数减一
     */
    @Override
    public boolean lowShopFollows(Integer shopId) {
        //更新数据库中的关注数
        boolean result = shopDao.lowShopFollows(shopId);
        if (!result) {
            throw new ShopException("商铺减少关注失败");
        }

        String cacheKey = SHOP_INFO_PREFIX + shopId;

        String cachedShopJson = RedisUtil.get(cacheKey);
        ShopsPO shop;
        if (cachedShopJson != null) {
            shop = new Gson().fromJson(cachedShopJson, ShopsPO.class);
        } else {
            shop = shopDao.showShopInfo(shopId);
            if (shop == null) {
                throw new ShopException("商铺信息不存在");
            }
        }

        int currentFollows = shop.getShopFollowers();
        if (currentFollows <= 0) {
            throw new ShopException("关注数不能为负数");
        }
        shop.setShopFollowers(currentFollows - 1);
        RedisUtil.set(cacheKey, new Gson().toJson(shop), CACHE_EXPIRE_SECONDS);

        return true;
    }

    /**
     * 商铺关注数加一
     */
    @Override
    public boolean incrementShopFollows(Integer shopId) {
        //更新数据库中的关注数
        boolean result = shopDao.incrementShopFollows(shopId);
        if (!result) {
            throw new ShopException("商铺增加关注失败");
        }

        String cacheKey = SHOP_INFO_PREFIX + shopId;

        String cachedShopJson = RedisUtil.get(cacheKey);
        ShopsPO shop;
        if (cachedShopJson != null) {
            shop = new Gson().fromJson(cachedShopJson, ShopsPO.class);
        } else {
            shop = shopDao.showShopInfo(shopId);
            if (shop == null) {
                throw new ShopException("商铺信息不存在");
            }
        }

        int currentFollows = shop.getShopFollowers();
        shop.setShopFollowers(currentFollows + 1);
        RedisUtil.set(cacheKey, new Gson().toJson(shop), CACHE_EXPIRE_SECONDS);

        return true;
    }
}