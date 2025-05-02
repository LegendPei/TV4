package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.dao.BlogDao;
import com.peitianbao.www.exception.BlogException;
import com.peitianbao.www.model.BlogCollection;
import com.peitianbao.www.model.Blogs;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.GsonFactory;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.List;
import java.util.Random;

/**
 * @author leg
 */
@Service
@DubboService
public class BlogService implements com.peitianbao.www.api.BlogService {
    @Autowired
    private BlogDao blogDao;

    private static final String BLOG_INFO_PREFIX = "blog:info:";
    private static final String USER_BLOGS_PREFIX = "user:blogs:";
    private static final String SHOP_BLOGS_PREFIX = "shop:blogs:";
    private static final String USER_COLLECTED_BLOGS_PREFIX = "user:collectedBlogs:";

    //基础缓存过期时间
    private static final int BASE_CACHE_EXPIRE_SECONDS = 3600;
    private static final int RANDOM_EXPIRE_OFFSET = 300;
    private static final int EMPTY_CACHE_EXPIRE_SECONDS = 300;

    private final Gson gson = GsonFactory.getGSON();

    /**
     * 创建动态
     */
    public boolean createBlog(Blogs blog) {
        boolean result = blogDao.createBlog(blog);
        if (result) {
            // 清理相关缓存
            RedisUtil.delete(BLOG_INFO_PREFIX + blog.getBlogId());
            RedisUtil.delete(USER_BLOGS_PREFIX + blog.getAuthorId() + ":time");
            RedisUtil.delete(USER_BLOGS_PREFIX + blog.getAuthorId() + ":likes");
            RedisUtil.delete(USER_BLOGS_PREFIX + blog.getAuthorId() + ":collections");

            return true;
        } else {
            throw new BlogException("创建动态失败");
        }
    }

    /**
     * 查询单个动态信息
     */
    public Blogs getBlogInfo(Integer blogId) {
        String cacheKey = BLOG_INFO_PREFIX + blogId;

        String cachedBlogJson = RedisUtil.get(cacheKey);
        Blogs blog;
        if (cachedBlogJson != null) {
            if ("NOT_EXISTS".equals(cachedBlogJson)) {
                throw new BlogException("动态不存在");
            }
            blog = gson.fromJson(cachedBlogJson, Blogs.class);
        } else {
            blog = blogDao.getBlogInfo(blogId);
            if (blog == null) {
                // 缓存空值，防止缓存穿透
                RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new BlogException("未查询到该动态");
            }

            // 设置缓存，并使用随机化过期时间
            RedisUtil.set(cacheKey, gson.toJson(blog), getRandomExpireTime());
        }

        return blog;
    }

    /**
     * 获取用户的动态列表
     */
    public List<Blogs> getUserBlogs(Integer userId, String sortMode) {
        if (!"likes".equals(sortMode) && !"time".equals(sortMode) && !"collections".equals(sortMode)) {
            throw new BlogException("输入的排序方法有误");
        }

        String cacheKey = USER_BLOGS_PREFIX + userId + ":" + sortMode;

        String cachedBlogsJson = RedisUtil.get(cacheKey);
        List<Blogs> blogs;
        if (cachedBlogsJson != null) {
            if ("[]".equals(cachedBlogsJson)) {
                throw new BlogException("该用户暂无动态");
            }
            blogs = gson.fromJson(cachedBlogsJson, new TypeToken<List<Blogs>>() {}.getType());
        } else {
            blogs = blogDao.getUserBlogs(userId, sortMode);
            if (blogs == null || blogs.isEmpty()) {
                // 缓存空值
                RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new BlogException("暂无该用户动态");
            }

            // 写入缓存，并使用随机化过期时间
            RedisUtil.set(cacheKey, gson.toJson(blogs), getRandomExpireTime());
        }

        return blogs;
    }

    /**
     * 获取商铺的动态列表
     */
    public List<Blogs> getShopBlogs(Integer shopId, String sortMode) {
        if (!"likes".equals(sortMode) && !"time".equals(sortMode) && !"collections".equals(sortMode)) {
            throw new BlogException("输入的排序方法有误");
        }

        String cacheKey = SHOP_BLOGS_PREFIX + shopId + ":" + sortMode;

        String cachedBlogsJson = RedisUtil.get(cacheKey);
        List<Blogs> blogs;
        if (cachedBlogsJson != null) {
            if ("[]".equals(cachedBlogsJson)) {
                throw new BlogException("该商铺暂无动态");
            }
            blogs = gson.fromJson(cachedBlogsJson, new TypeToken<List<Blogs>>() {}.getType());
        } else {
            blogs = blogDao.getShopBlogs(shopId, sortMode);
            if (blogs == null || blogs.isEmpty()) {
                RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new BlogException("该商铺暂无动态");
            }

            RedisUtil.set(cacheKey, gson.toJson(blogs), getRandomExpireTime());
        }

        return blogs;
    }

    /**
     * 用户收藏动态
     */
    public boolean collectBlog(Integer blogId,Integer userId) {
        boolean result = blogDao.collectBlog(blogId,userId);
        if (result) {
            String cacheKey = BLOG_INFO_PREFIX + blogId;

            Blogs blog = blogDao.getBlogInfo(blogId);
            if (blog == null) {
                throw new BlogException("动态不存在");
            }

            blog.setBlogCollections(blog.getBlogCollections() + 1);

            RedisUtil.set(cacheKey, gson.toJson(blog), getRandomExpireTime());

            RedisUtil.delete(USER_COLLECTED_BLOGS_PREFIX + blogId);

            return true;
        } else {
            throw new BlogException("收藏动态失败");
        }
    }

    /**
     * 用户取消收藏动态
     */
    public boolean unCollectBlog(Integer blogId,Integer userId) {
        boolean result = blogDao.unCollectBlog(blogId,userId);
        if (result) {
            String cacheKey = BLOG_INFO_PREFIX + blogId;

            Blogs blog = blogDao.getBlogInfo(blogId);
            if (blog == null) {
                throw new BlogException("动态不存在");
            }

            if (blog.getBlogCollections() > 0) {
                blog.setBlogCollections(blog.getBlogCollections() - 1);
            } else {
                blog.setBlogCollections(0);
            }

            RedisUtil.set(cacheKey, gson.toJson(blog), getRandomExpireTime());

            RedisUtil.delete(USER_COLLECTED_BLOGS_PREFIX + blogId);

            return true;
        } else {
            throw new BlogException("取消收藏动态失败");
        }
    }

    /**
     * 获取用户收藏的动态列表
     */
    public List<BlogCollection> getUserCollectBlogs(Integer userId) {
        String cacheKey = USER_COLLECTED_BLOGS_PREFIX + userId;

        String cachedJson = RedisUtil.get(cacheKey);
        List<BlogCollection> collections;

        if (cachedJson != null) {
            if ("[]".equals(cachedJson)) {
                throw new BlogException("用户暂无收藏动态");
            }
            collections = gson.fromJson(cachedJson, new TypeToken<List<BlogCollection>>() {}.getType());
        } else {
            collections = blogDao.getUserCollectBlogs(userId);

            if (collections == null || collections.isEmpty()) {
                RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                throw new BlogException("用户暂无收藏动态");
            }

            RedisUtil.set(cacheKey, gson.toJson(collections), getRandomExpireTime());
        }
        return collections;
    }

    /**
     * 增加动态点赞数
     */
    @Override
    public boolean incrementBlogLikes(Integer id) {
        String cacheKey = BLOG_INFO_PREFIX + id;

        String cachedBlogJson = RedisUtil.get(cacheKey);
        Blogs blog;
        if (cachedBlogJson != null) {
            if ("NOT_EXISTS".equals(cachedBlogJson)) {
                throw new BlogException("动态不存在");
            }
            blog = gson.fromJson(cachedBlogJson, Blogs.class);
        } else {
            blog = blogDao.getBlogInfo(id);
            if (blog == null) {
                throw new BlogException("动态不存在");
            }
        }

        blog.setBlogLikes(blog.getBlogLikes() + 1);
        boolean result = blogDao.incrementBlogLikes(id);
        if (result) {
            RedisUtil.set(cacheKey, gson.toJson(blog), getRandomExpireTime());
            return true;
        } else {
            throw new BlogException("增加动态点赞失败");
        }
    }

    /**
     * 减少动态点赞数
     */
    @Override
    public boolean lowBlogLikes(Integer id) {
        String cacheKey = BLOG_INFO_PREFIX + id;

        String cachedBlogJson = RedisUtil.get(cacheKey);
        Blogs blog;
        if (cachedBlogJson != null) {
            if ("NOT_EXISTS".equals(cachedBlogJson)) {
                throw new BlogException("动态不存在");
            }
            blog = gson.fromJson(cachedBlogJson, Blogs.class);
        } else {
            blog = blogDao.getBlogInfo(id);
            if (blog == null) {
                throw new BlogException("动态不存在");
            }
        }

        if (blog.getBlogLikes() <= 0) {
            throw new BlogException("点赞数不能为负数");
        }

        blog.setBlogLikes(blog.getBlogLikes() - 1);
        boolean result = blogDao.decrementBlogLikes(id);
        if (result) {
            RedisUtil.set(cacheKey, gson.toJson(blog), getRandomExpireTime());
            return true;
        } else {
            throw new BlogException("减少动态点赞失败");
        }
    }

    /**
     * 获取随机化的缓存过期时间
     */
    private int getRandomExpireTime() {
        return BASE_CACHE_EXPIRE_SECONDS + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
    }
}
