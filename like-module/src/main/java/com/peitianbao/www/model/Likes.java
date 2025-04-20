package com.peitianbao.www.model;

import com.peitianbao.www.mybatis.annotation.Column;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
public class Likes {
    private Integer targetId;
    private Integer likerId;
    private LocalDateTime likeTime;

    public Likes(Integer targetId, Integer likerId) {
        this.targetId = targetId;
        this.likerId = likerId;
    }
}
