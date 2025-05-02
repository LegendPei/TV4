package com.peitianbao.www.dao;

import com.peitianbao.www.model.Likes;

import java.util.List;

/**
 * @author leg
 */
public interface LikeDao {
    /**
     * 插入商铺点赞
     * @param likes 插入的点赞信息
     * @return 是否成功插入
     */
    boolean insertShopLike(Likes likes);

    /**
     * 插入评论点赞
     * @param likes 插入的点赞信息
     * @return 是否成功插入
     */
    boolean insertCommentLike(Likes likes);

    /**
     * 插入动态点赞
     * @param likes 插入的点赞信息
     * @return 是否成功插入
     */
    boolean insertBlogLike(Likes likes);

    /**
     * 查询给商铺点赞的记录
     * @param shopId 商铺id
     * @return 点赞信息的集合
     */
    List<Likes> selectShopLikes(Integer shopId);

    /**
     * 查询给动态点赞的记录
     * @param blogId 动态id
     * @return 点赞信息的集合
     */
    List<Likes> selectBlogLikes(Integer blogId);

    /**
     * 查询给评论点赞的记录
     * @param commentId 评论id
     * @return 点赞信息的集合
     */
    List<Likes> selectCommentLikes(Integer commentId);

    /**
     * 查询用户的点赞商铺记录
     * @param userId 用户id
     * @return 点赞信息的集合
     */
    List<Likes> selectUserLikesShops(Integer userId);

    /**
     * 查询用户的点赞评论记录
     * @param userId 用户id
     * @return 点赞信息的集合
     */
    List<Likes> selectUserLikesComments(Integer userId);
}
