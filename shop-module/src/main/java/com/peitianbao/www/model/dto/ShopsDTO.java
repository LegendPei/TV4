package com.peitianbao.www.model.dto;

import com.peitianbao.www.model.po.ShopsPO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShopsDTO {
    private Integer shopId;
    private String shopName;
    private String shopAccount;
    private String shopAddress;
    private String shopInfo;
    private int shopLikes;
    private int shopFollowers;

    public ShopsDTO(ShopsPO shopsPo) {
        this.shopId = shopsPo.getShopId();
        this.shopName = shopsPo.getShopName();
        this.shopAccount = shopsPo.getShopAccount();
        this.shopAddress = shopsPo.getShopAddress();
        this.shopInfo = shopsPo.getShopInfo();
        this.shopLikes = shopsPo.getShopLikes();
        this.shopFollowers = shopsPo.getShopFollowers();
    }
}
