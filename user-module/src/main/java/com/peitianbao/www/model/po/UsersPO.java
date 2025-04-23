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
public class UsersPO implements Serializable {
    @Column("user_id")
    private Integer userId;
    @Column("user_name")
    private String userName;
    @Column("user_account")
    private String userAccount;
    @Column("user_password")
    private String userPassword;

    private int followers;
    private int followingUsers;
    private int followingShops;
}
