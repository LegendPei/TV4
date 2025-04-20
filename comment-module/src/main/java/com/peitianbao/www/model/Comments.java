package com.peitianbao.www.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * @author leg
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comments {
    private Integer commentId;
    private Integer commenterId;
    private Integer targetId;
    private String commentContent;
    private LocalDateTime commentTime;
    private int commentLikes;
}
