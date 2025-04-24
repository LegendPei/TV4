package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.api.ShopService;
import com.peitianbao.www.dao.LikeDao;
import com.peitianbao.www.exception.LikeException;
import com.peitianbao.www.model.Likes;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.List;

/**
 * @author leg
 */
@Service
public class LikeService {

    @Autowired
    private LikeDao likeDao;

    @Autowired
    private ShopService shopService;

    //缓存前缀
    private static final String SHOP_LIKES_PREFIX = "like:shop:";
    private static final String COMMENT_LIKES_PREFIX = "like:comment:";

    //缓存过期时间（单位：秒）
    private static final int CACHE_EXPIRE_SECONDS = 3600;

    /**
     * 商铺插入点赞
     */
    public boolean shopLike(Integer targetId, Integer likerId) {
        Likes likes = new Likes(targetId, likerId);

        boolean result = likeDao.insertShopLike(likes);

        if (result) {
            //更新缓存
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
            //更新缓存
            updateLikesCache(targetId);
            return true;
        } else {
            throw new LikeException("评论点赞失败");
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
                //从缓存中获取点赞列表
                likesList = new Gson().fromJson(cachedLikesJson, new TypeToken<List<Likes>>() {}.getType());
            } else {
                //从数据库中加载点赞列表
                likesList = likeDao.selectShopLikes(shopId);
                if (likesList != null) {
                    // 写入缓存
                    RedisUtil.set(cacheKey, new Gson().toJson(likesList), CACHE_EXPIRE_SECONDS);
                }
            }

            if (likesList == null || likesList.isEmpty()) {
                throw new LikeException("商铺暂无点赞记录");
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
                // 从缓存中获取点赞列表
                likesList = new Gson().fromJson(cachedLikesJson, new TypeToken<List<Likes>>() {}.getType());
            } else {
                // 从数据库中加载点赞列表
                likesList = likeDao.selectCommentLikes(commentId);
                if (likesList != null) {
                    // 写入缓存
                    RedisUtil.set(cacheKey, new Gson().toJson(likesList), CACHE_EXPIRE_SECONDS);
                }
            }

            if (likesList == null || likesList.isEmpty()) {
                throw new LikeException("评论暂无点赞记录");
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
            // 更新评论点赞缓存
            cacheKey = COMMENT_LIKES_PREFIX + targetId;
            likesList = likeDao.selectCommentLikes(targetId);
        } else {
            // 更新商铺点赞缓存
            cacheKey = SHOP_LIKES_PREFIX + targetId;
            likesList = likeDao.selectShopLikes(targetId);
        }
        RedisUtil.set(cacheKey, new Gson().toJson(likesList), CACHE_EXPIRE_SECONDS);
    }
}