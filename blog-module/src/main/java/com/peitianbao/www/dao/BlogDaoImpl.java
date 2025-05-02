package com.peitianbao.www.dao;

import com.peitianbao.www.exception.BlogException;
import com.peitianbao.www.model.BlogCollection;
import com.peitianbao.www.model.Blogs;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Dao;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Dao
public class BlogDaoImpl implements BlogDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    public boolean createBlog(Blogs blog) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("blogName", blog.getBlogName());
            params.put("targetId", blog.getTargetId());
            params.put("authorId", blog.getAuthorId());
            params.put("blogContent", blog.getBlogName());
            params.put("filePath", blog.getTargetId());
            params.put("blogType", blog.getAuthorId());

            LoggingFramework.info("尝试创建动态：" + blog.getBlogName());
            int result = sqlSession.executeUpdate("BlogMapper.insertBlog", params);
            LoggingFramework.info("动态插入成功：" + blog.getBlogName());
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入动态失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new BlogException("插入动态失败", e);
        }
    }

    @Override
    public Blogs getBlogInfo(Integer blogId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("blogId", blogId);

            return sqlSession.executeQueryForObject("BlogMapper.selectBlogById", params, Blogs.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询动态失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Blogs> getUserBlogs(Integer userId, String sortMode) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("authorId", userId);

            List<Blogs> blogs = sqlSession.executeQueryForList("BlogMapper.selectBlogsByUserId", params, Blogs.class);

            if (blogs != null && !blogs.isEmpty()) {
                LoggingFramework.info("查询到 " + blogs.size() + " 条动态");
            } else {
                LoggingFramework.warning("未找到，ID：" + userId);
            }

            return sortBlogs(blogs, sortMode);
        } catch (Exception e) {
            LoggingFramework.severe("查询动态失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Blogs> getShopBlogs(Integer shopId, String sortMode) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("authorId", shopId);

            List<Blogs> blogs = sqlSession.executeQueryForList("BlogMapper.selectBlogsByShopId", params, Blogs.class);

            if (blogs != null && !blogs.isEmpty()) {
                LoggingFramework.info("查询到 " + blogs.size() + " 条动态");
            } else {
                LoggingFramework.warning("未找到，ID：" + shopId);
            }

            return sortBlogs(blogs, sortMode);
        } catch (Exception e) {
            LoggingFramework.severe("查询动态失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public boolean incrementBlogLikes(Integer blogId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("blogId", blogId);

            LoggingFramework.info("尝试更新动态点赞数：blogId = " + blogId);
            int result = sqlSession.executeUpdate("BlogMapper.incrementBlogLikes", params);
            if (result > 0) {
                LoggingFramework.info("动态点赞数更新成功：blogId = " + blogId);
            } else {
                LoggingFramework.warning("动态点赞数更新失败：blogId = " + blogId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("更新动态点赞数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new BlogException("更新动态点赞数失败", e);
        }
    }

    @Override
    public boolean decrementBlogLikes(Integer blogId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("blogId", blogId);

            LoggingFramework.info("尝试更新动态点赞数：blogId = " + blogId);
            int result = sqlSession.executeUpdate("BlogMapper.lowBlogLikes", params);
            if (result > 0) {
                LoggingFramework.info("动态点赞数更新成功：blogId = " + blogId);
            } else {
                LoggingFramework.warning("动态点赞数更新失败：blogId = " + blogId);
            }
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("更新动态点赞数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new BlogException("更新动态点赞数失败", e);
        }
    }

    @Override
    public boolean collectBlog(Integer blogId,Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> param = new HashMap<>();
            params.put("blogId", blogId);
            params.put("userId", userId);
            param.put("blogId", blogId);

            int result = sqlSession.executeUpdate("BlogMapper.incrementBlogCollections", param);
            int result1=sqlSession.executeUpdate("BlogMapper.insertBlogCollection", params);

            return result!=0&&result1!=0;
        } catch (Exception e) {
            LoggingFramework.severe("更新动态收藏数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new BlogException("更新动态收藏数失败", e);
        }
    }

    @Override
    public boolean unCollectBlog(Integer blogId,Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            Map<String, Object> param = new HashMap<>();
            params.put("blogId", blogId);
            params.put("userId", userId);
            param.put("blogId", blogId);

            int result = sqlSession.executeUpdate("BlogMapper.lowBlogCollections", param);
            int result1=sqlSession.executeUpdate("BlogMapper.deleteBlogCollection", params);

            return result!=0&&result1!=0;
        } catch (Exception e) {
            LoggingFramework.severe("更新动态收藏数失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new BlogException("更新动态收藏数失败", e);
        }
    }

    @Override
    public List<BlogCollection> getUserCollectBlogs(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("userId", userId);

            List<BlogCollection> blogs = sqlSession.executeQueryForList("BlogMapper.selectUserCollectBlogs", params, BlogCollection.class);

            if (blogs != null && !blogs.isEmpty()) {
                LoggingFramework.info("查询到 " + blogs.size() + " 条动态");
            } else {
                LoggingFramework.warning("未找到，ID：" + userId);
            }

            return blogs;
        } catch (Exception e) {
            LoggingFramework.severe("查询动态失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
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
