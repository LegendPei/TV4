package com.peitianbao.www.api;

/**
 * @author leg
 */
public interface ShopService {
    /**
     * 更新商铺点赞数
     * @param shopId 商铺 ID
     * @return 是否更新成功
     */
    boolean incrementShopLikes(Integer shopId);

    /**
     * 更新商铺点赞数
     * @param shopId 商铺 ID
     * @return 是否更新成功
     */
    boolean lowShopLikes(Integer shopId);

    /**
     * 更新商铺关注数
     * @param shopId 商铺 ID
     * @return 是否更新成功
     */
    boolean incrementShopFollows(Integer shopId);

    /**
     * 更新商铺点赞数
     * @param shopId 商铺 ID
     * @return 是否更新成功
     */
    boolean lowShopFollows(Integer shopId);
}
