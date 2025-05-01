package com.peitianbao.www.dao;

import com.peitianbao.www.model.Blogs;

import java.util.List;

/**
 * @author leg
 */
public interface BlogDao {
    /**
     * 创建动态
     * @param blog 动态
     * @return 是否创建成功
     */
    boolean createBlog(Blogs blog);

    /**
     * 得到动态信息
     * @param blogId 动态id
     * @return 该动态
     */
    Blogs getBlogInfo(Integer blogId);

    /**
     * 得到该用户的动态,并排序
     * @param userId 用户id
     * @param sortMode 排序方式
     * @return 动态集合
     */
    List<Blogs> getUserBlogs(Integer userId,String sortMode);

    /**
     * 得到该商铺的动态
     * @param shopId 商铺id
     * @param sortMode 排序方式
     * @return 动态集合
     */
    List<Blogs> getShopBlogs(Integer shopId,String sortMode);

    /**
     * 增加动态点赞数
     * @param blogId 动态id
     * @return 是否增加成功
     */
    boolean incrementBlogLikes(Integer blogId);

    /**
     * 减少动态点赞数
     * @param blogId 动态id
     * @return 是否减少成功
     */
    boolean decrementBlogLikes(Integer blogId);

    /**
     * 收藏动态
     * @param blogId 动态id
     * @return 是否收藏成功
     */
    boolean collectBlog(Integer blogId);

    /**
     * 取消收藏动态
     * @param blogId 动态id
     * @return 是否取消成功
     */
    boolean unCollectBlog(Integer blogId);

    /**
     * 得到用户收藏的动态
     * @param userId 用户id
     * @param sortMode 排序方式
     * @return 动态集合
     */
    List<Blogs> getUserCollectBlogs(Integer userId,String sortMode);
}
