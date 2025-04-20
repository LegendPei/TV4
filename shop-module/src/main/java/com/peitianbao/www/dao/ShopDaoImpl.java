package com.peitianbao.www.dao;

import com.peitianbao.www.exception.ShopException;
import com.peitianbao.www.model.po.ShopsPO;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Dao;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;
import org.mindrot.jbcrypt.BCrypt;

import java.util.HashMap;
import java.util.Map;

/**
 * @author leg
 */
@Dao
public class ShopDaoImpl implements ShopDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    @LoggingFramework.Log(message = "商铺注册操作")
    public boolean insertShop(ShopsPO shop) {
        try {
            String hashedPassword = BCrypt.hashpw(shop.getShopPassword(), BCrypt.gensalt());

            Map<String, Object> params = new HashMap<>();
            params.put("shopName", shop.getShopName());
            params.put("shopAccount", shop.getShopAccount());
            params.put("shopPassword", hashedPassword);
            params.put("shopInfo", shop.getShopInfo());
            params.put("shopAddress", shop.getShopAddress());

            LoggingFramework.info("尝试插入商铺：" + shop.getShopName());
            int result = sqlSession.executeUpdate("ShopMapper.insertShop", params);
            LoggingFramework.info("商铺插入成功：" + shop.getShopName());
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入商铺失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new ShopException("插入商铺失败", e);
        }
    }

    @Override
    public boolean updateShop(ShopsPO shop) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("shopId", shop.getShopId());
            if (shop.getShopName() != null && !shop.getShopName().isEmpty()) {
                params.put("shopName", shop.getShopName());
            }
            if (shop.getShopAccount() != null && !shop.getShopAccount().isEmpty()) {
                params.put("shopAccount", shop.getShopAccount());
            }
            if (shop.getShopPassword() != null && !shop.getShopPassword().isEmpty()) {
                String hashedPassword = BCrypt.hashpw(shop.getShopPassword(), BCrypt.gensalt());
                params.put("shopPassword", hashedPassword);
            }
            if (shop.getShopInfo() != null && !shop.getShopInfo().isEmpty()) {
                params.put("shopInfo", shop.getShopInfo());
            }
            if (shop.getShopAddress() != null && !shop.getShopAddress().isEmpty()) {
                params.put("shopAddress", shop.getShopAddress());
            }

            LoggingFramework.info("尝试更新商铺 ID：" + shop.getShopId());
            int result = sqlSession.executeUpdate("ShopMapper.updateShop", params);
            LoggingFramework.info("商铺更新成功：" + shop.getShopName());
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("更新商铺失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new ShopException("更新商铺失败", e);
        }
    }

    @Override
    public boolean deleteShop(Integer shopId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("shopId", shopId);

            LoggingFramework.info("尝试删除商铺 ID：" + shopId);
            int result = sqlSession.executeUpdate("ShopMapper.deleteShopById", params);
            LoggingFramework.info("商铺删除成功：" + shopId);
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("删除商铺失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new ShopException("删除商铺失败", e);
        }
    }

    @Override
    public ShopsPO loginShop(String shopAccount, String shopPassword) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("shopAccount", shopAccount);

            LoggingFramework.info("尝试查询商铺，账号：" + shopAccount);

            ShopsPO shop = sqlSession.executeQueryForObject(
                    "ShopMapper.selectShopByAccountAndPassword",
                    params,
                    ShopsPO.class
            );

            if (shop == null) {
                LoggingFramework.warning("未找到商铺，账号：" + shopAccount);
                return null;
            }

            String hashedPassword = shop.getShopPassword();

            if (BCrypt.checkpw(shopPassword, hashedPassword)) {
                LoggingFramework.info("查询到商铺：" + shop.getShopName());
                return shop;
            } else {
                LoggingFramework.warning("商铺密码错误，账号：" + shopAccount);
                return null;
            }
        } catch (Exception e) {
            LoggingFramework.severe("查询商铺失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public ShopsPO showShopInfo(Integer shopId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("shopId", shopId);

            LoggingFramework.info("尝试查询商铺 ID：" + shopId);
            ShopsPO shop = sqlSession.executeQueryForObject("ShopMapper.selectShopById", params, ShopsPO.class);

            if (shop != null) {
                LoggingFramework.info("查询到商铺：" + shop.getShopName());
            } else {
                LoggingFramework.warning("未找到商铺 ID：" + shopId);
            }
            return shop;
        } catch (Exception e) {
            LoggingFramework.severe("查询商铺失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public Integer findShopIdByAccount(String shopAccount) {
        try {
            //准备参数
            Map<String, Object> params = new HashMap<>();
            params.put("account", shopAccount);

            //执行查询
            ShopsPO shop = sqlSession.executeQueryForObject("ShopMapper.selectShopByAccount",params,ShopsPO.class);

            return shop.getShopId();
        } catch (Exception e) {
            LoggingFramework.severe("搜索商铺失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public boolean incrementShopLikes(Integer shopId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("shopId", shopId);

            LoggingFramework.info("尝试更新商铺点赞数：shopId = " + shopId);
            int result = sqlSession.executeUpdate("ShopMapper.incrementShopLikes", params);
            if (result > 0) {
                LoggingFramework.info("商铺点赞数更新成功：shopId = " + shopId);
            } else {
                LoggingFramework.warning("商铺点赞数更新失败：shopId = " + shopId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("更新商铺点赞数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new RuntimeException("更新商铺点赞数失败", e);
        }
    }
}
