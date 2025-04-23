package com.peitianbao.www.model.dto;

import com.peitianbao.www.model.po.UsersPO;
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
public class UsersDTO implements Serializable {
    private Integer userId;
    private String userName;
    private String userAccount;
    private int followers;
    private int followingUsers;
    private int followingShops;

    public UsersDTO(UsersPO userPo) {
        this.userId = userPo.getUserId();
        this.userName = userPo.getUserName();
        this.userAccount = userPo.getUserAccount();
        this.followers = userPo.getFollowers();
        this.followingUsers = userPo.getFollowingUsers();
        this.followingShops = userPo.getFollowingShops();
    }
}
