package com.peitianbao.www.model;

import com.peitianbao.www.mybatis.annotation.Column;
import com.peitianbao.www.mybatis.annotation.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("follows")
public class Follows {
    @Column("target_id")
    private Integer targetId;
    @Column("follower_id")
    private Integer followerId;
}
