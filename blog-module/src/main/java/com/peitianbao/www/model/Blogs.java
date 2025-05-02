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
@AllArgsConstructor
@NoArgsConstructor
public class Blogs implements Serializable {
    private Integer blogId;
    private Integer targetId;
    private String blogName;
    private Integer authorId;
    private String blogContent;
    private LocalDateTime blogTime;
    private int blogLikes;
    private int blogCollections;
    private String filePath;
    //1是美食推荐，2是商铺推荐，3是秒杀分享
    private int blogType;

    public Blogs(Integer targetId,String blogName,Integer authorId,String blogContent,String filePath,int blogType){
        this.targetId = targetId;
        this.blogName = blogName;
        this.authorId = authorId;
        this.blogContent = blogContent;
        this.filePath = filePath;
        this.blogType = blogType;
    }
}
