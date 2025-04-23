package com.peitianbao.www.model.po;

import com.peitianbao.www.mybatis.annotation.Column;
import com.peitianbao.www.mybatis.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("users")
public class ShopsPO implements Serializable {
    @Column("shop_id")
    private Integer shopId;
    @Column("shop_name")
    private String shopName;
    @Column("shop_account")
    private String shopAccount;
    @Column("shop_password")
    private String shopPassword;
    @Column("shop_address")
    private String shopAddress;
    @Column("shop_info")
    private String shopInfo;
    @Column("shop_likes")
    private int shopLikes;

    private int shopFollowers;
}
