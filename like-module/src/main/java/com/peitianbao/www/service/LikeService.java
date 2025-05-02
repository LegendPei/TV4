package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.api.ShopService;
import com.peitianbao.www.dao.LikeDao;
import com.peitianbao.www.exception.LikeException;
import com.peitianbao.www.model.Likes;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.GsonFactory;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.List;
import java.util.Random;

/**
 * @author leg
 */
@Service
public class LikeService {

    @Autowired
    private LikeDao likeDao;

    private static final String SHOP_LIKES_PREFIX = "like:shop:";
    private static final String COMMENT_LIKES_PREFIX = "like:comment:";
    private static final String BLOG_LIKES_PREFIX = "like:blog:";

    //基础缓存过期时间
    private static final int BASE_CACHE_EXPIRE_SECONDS = 3600;

    //随机化缓存过期时间的最大偏移量
    private static final int RANDOM_EXPIRE_OFFSET = 300;

    //缓存空值的过期时间
    private static final int EMPTY_CACHE_EXPIRE_SECONDS = 300;

    private final Gson gson = GsonFactory.getGSON();

    /**
     * 商铺插入点赞
     */
    public boolean shopLike(Integer targetId, Integer likerId) {
        Likes likes = new Likes(targetId, likerId);

        boolean result = likeDao.insertShopLike(likes);
        if (result) {
            updateLikesCache(targetId);
            return true;
        } else {
            throw new LikeException("商铺点赞失败");
        }
    }

    /**
     * 评论插入点赞
     */
    public boolean commentLike(Integer targetId, Integer likerId) {
        Likes likes = new Likes(targetId, likerId);

        boolean result = likeDao.insertCommentLike(likes);
        if (result) {
            updateLikesCache(targetId);
            return true;
        } else {
            throw new LikeException("评论点赞失败");
        }
    }

    /**
     * 动态插入点赞
     */
    public boolean blogLike(Integer targetId, Integer likerId) {
        Likes likes = new Likes(targetId, likerId);

        boolean result = likeDao.insertBlogLike(likes);
        if (result) {
            updateLikesCache(targetId);
            return true;
        } else {
            throw new LikeException("动态点赞失败");
        }
    }

    /**
     * 查询商铺点赞列表
     */
    public List<Likes> selectShopLikes(Integer shopId) {
        if (shopId >= 200000) {
            String cacheKey = SHOP_LIKES_PREFIX + shopId;
            String cachedLikesJson = RedisUtil.get(cacheKey);

            List<Likes> likesList;
            if (cachedLikesJson != null) {
                likesList = gson.fromJson(cachedLikesJson, new TypeToken<List<Likes>>() {}.getType());
            } else {
                likesList = likeDao.selectShopLikes(shopId);
                if (likesList == null || likesList.isEmpty()) {
                    RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                    throw new LikeException("商铺暂无点赞记录");
                }
                RedisUtil.set(cacheKey, gson.toJson(likesList), getRandomExpireTime());
            }
            return likesList;
        } else {
            throw new LikeException("搜索商铺点赞失败");
        }
    }

    /**
     * 查询评论点赞列表
     */
    public List<Likes> selectCommentLikes(Integer commentId) {
        if (commentId < 100000) {
            String cacheKey = COMMENT_LIKES_PREFIX + commentId;
            String cachedLikesJson = RedisUtil.get(cacheKey);

            List<Likes> likesList;
            if (cachedLikesJson != null) {
                likesList = gson.fromJson(cachedLikesJson, new TypeToken<List<Likes>>() {}.getType());
            } else {
                likesList = likeDao.selectCommentLikes(commentId);
                if (likesList == null || likesList.isEmpty()) {
                    RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                    throw new LikeException("评论暂无点赞记录");
                }
                RedisUtil.set(cacheKey, gson.toJson(likesList), getRandomExpireTime());
            }
            return likesList;
        } else {
            throw new LikeException("搜索评论点赞失败");
        }
    }

    /**
     * 查询动态点赞列表
     */
    public List<Likes> selectBlogLikes(Integer blogId) {
        if (blogId < 400000) {
            String cacheKey = BLOG_LIKES_PREFIX + blogId;
            String cachedLikesJson = RedisUtil.get(cacheKey);

            List<Likes> likesList;
            if (cachedLikesJson != null) {
                likesList = gson.fromJson(cachedLikesJson, new TypeToken<List<Likes>>() {}.getType());
            } else {
                likesList = likeDao.selectBlogLikes(blogId);
                if (likesList == null || likesList.isEmpty()) {
                    RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                    throw new LikeException("动态暂无点赞记录");
                }
                RedisUtil.set(cacheKey, gson.toJson(likesList), getRandomExpireTime());
            }
            return likesList;
        } else {
            throw new LikeException("搜索评论点赞失败");
        }
    }

    /**
     * 查询用户点赞的商铺列表
     */
    public List<Likes> selectUserLikesShops(Integer userId) {
        if (userId >= 100000 && userId < 200000) {
            return likeDao.selectUserLikesShops(userId);
        } else {
            throw new LikeException("搜索用户点赞店铺失败");
        }
    }

    /**
     * 查询用户点赞的评论列表
     */
    public List<Likes> selectUserLikesComments(Integer userId) {
        if (userId >= 100000 && userId < 200000) {
            return likeDao.selectUserLikesComments(userId);
        } else {
            throw new LikeException("搜索用户点赞评论失败");
        }
    }

    /**
     * 更新缓存
     */
    private void updateLikesCache(Integer targetId) {
        String cacheKey;
        List<Likes> likesList;
        if (targetId < 100000) {
            cacheKey = COMMENT_LIKES_PREFIX + targetId;
            likesList = likeDao.selectCommentLikes(targetId);
        } else if (targetId >=400000) {
            cacheKey = BLOG_LIKES_PREFIX + targetId;
            likesList = likeDao.selectBlogLikes(targetId);
        } else {
            cacheKey = SHOP_LIKES_PREFIX + targetId;
            likesList = likeDao.selectShopLikes(targetId);
        }

        if (likesList == null || likesList.isEmpty()) {
            RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
        } else {
            RedisUtil.set(cacheKey, gson.toJson(likesList), getRandomExpireTime());
        }
    }

    /**
     * 获取随机化的缓存过期时间
     */
    private int getRandomExpireTime() {
        return BASE_CACHE_EXPIRE_SECONDS + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
    }
}