package com.peitianbao.www.model;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
public class Likes implements Serializable {
    private Integer targetId;
    private Integer likerId;
    private LocalDateTime likeTime;

    public Likes(Integer targetId, Integer likerId) {
        this.targetId = targetId;
        this.likerId = likerId;
    }
}
