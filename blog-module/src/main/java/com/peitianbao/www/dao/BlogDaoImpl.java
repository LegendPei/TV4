package com.peitianbao.www.dao;

import com.peitianbao.www.model.Blogs;
import com.peitianbao.www.springframework.annontion.Dao;

import java.util.Comparator;
import java.util.List;

/**
 * @author leg
 */
@Dao
public class BlogDaoImpl implements BlogDao {
    @Override
    public boolean createBlog(Blogs blog) {
        return false;
    }

    @Override
    public Blogs getBlogInfo(Integer blogId) {
        return null;
    }

    @Override
    public List<Blogs> getUserBlogs(Integer userId, String sortMode) {
        return List.of();
    }

    @Override
    public List<Blogs> getShopBlogs(Integer shopId, String sortMode) {
        return List.of();
    }


    @Override
    public boolean incrementBlogLikes(Integer blogId) {
        return false;
    }

    @Override
    public boolean decrementBlogLikes(Integer blogId) {
        return false;
    }

    @Override
    public boolean collectBlog(Integer blogId) {
        return false;
    }

    @Override
    public boolean unCollectBlog(Integer blogId) {
        return false;
    }

    @Override
    public List<Blogs> getUserCollectBlogs(Integer userId, String sortMode) {
        return List.of();
    }


    private List<Blogs> sortBlogs(List<Blogs> blogs, String sortMode) {
        if ("time".equalsIgnoreCase(sortMode)) {
            //按时间排序（降序）
            blogs.sort(Comparator.comparing(Blogs::getBlogTime).reversed());
        } else if ("likes".equalsIgnoreCase(sortMode)) {
            //按点赞数排序（降序），点赞数相同则按时间排序（降序）
            blogs.sort(Comparator.comparing(Blogs::getBlogLikes)
                    .thenComparing(Blogs::getBlogTime).reversed());
        }else if("collections".equalsIgnoreCase(sortMode)){
            //按收藏数排序（降序），点赞数相同则按时间排序（降序）
            blogs.sort(Comparator.comparing(Blogs::getBlogCollections)
                    .thenComparing(Blogs::getBlogTime).reversed());
        }else {
            throw new IllegalArgumentException("无效的排序模式: " + sortMode);
        }
        return blogs;
    }
}
