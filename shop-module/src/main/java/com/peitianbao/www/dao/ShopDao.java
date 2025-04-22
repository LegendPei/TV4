package com.peitianbao.www.dao;

import com.peitianbao.www.model.po.ShopsPO;

import java.util.List;

/**
 * @author leg
 */
public interface ShopDao {
    /**
     * 插入商铺
     * @param shop 插入的商铺
     * @return 是否插入成功
     */
    boolean insertShop(ShopsPO shop);

    /**
     * 更新商铺信息
     * @param shop 要更新的商铺信息
     * @return 是否更新成功
     */
    boolean updateShop(ShopsPO shop);

    /**
     * 删除商铺
     * @param shopId 删除的商铺id
     * @return 是否删除成功
     */
    boolean deleteShop(Integer shopId);

    /**
     * 商铺登录
     * @param shopAccount 商铺账户
     * @param shopPassword 商铺密码
     * @return 登录商铺的实体类
     */
    ShopsPO loginShop(String shopAccount, String shopPassword);

    /**
     * 展示商铺信息
     * @param shopId 商铺id
     * @return 商铺的实体类
     */
    ShopsPO showShopInfo(Integer shopId);

    /**
     * 通过商铺账号查找商铺id
     * @param shopAccount 商铺账号
     * @return 商铺id
     */
    Integer findShopIdByAccount(String shopAccount);

    /**
     * 根据商铺id增加其的点赞数
     * @param shopId 商铺id
     * @return 是否增加成功
     */
    boolean incrementShopLikes(Integer shopId);

    /**
     * 根据商铺id减少其的点赞数
     * @param shopId 商铺id
     * @return 是否增加成功
     */
    boolean lowShopLikes(Integer shopId);

    /**
     * 根据商铺id增加其的关注数
     * @param shopId 商铺id
     * @return 是否增加成功
     */
    boolean incrementShopFollows(Integer shopId);

    /**
     * 根据商铺id减少其的关注数
     * @param shopId 商铺id
     * @return 是否增加成功
     */
    boolean lowShopFollows(Integer shopId);

    /**
     * 查询所有商铺
     * @return 所有商铺集合
     */
    List<ShopsPO> findAllShops();
}
