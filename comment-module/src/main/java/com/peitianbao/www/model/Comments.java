package com.peitianbao.www.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments implements Serializable {
    private Integer commentId;
    private Integer commenterId;
    private Integer targetId;
    private String commentContent;
    private LocalDateTime commentTime;
    private int commentLikes;

    public Comments(Integer targetId,Integer commenterId,String commentContent){
        this.targetId = targetId;
        this.commenterId = commenterId;
        this.commentContent = commentContent;
    }
}
