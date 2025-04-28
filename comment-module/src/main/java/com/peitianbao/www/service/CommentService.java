package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.dao.CommentDao;
import com.peitianbao.www.exception.CommentException;
import com.peitianbao.www.model.Comments;
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
public class CommentService implements com.peitianbao.www.api.CommentService {

    @Autowired
    private CommentDao commentDao;

    private static final String COMMENT_INFO_PREFIX = "comment:info:";
    private static final String COMMENTS_BY_TARGET_PREFIX = "comments:byTarget:";

    //基础缓存过期时间
    private static final int BASE_CACHE_EXPIRE_SECONDS = 3600;

    //随机化缓存过期时间的最大偏移量
    private static final int RANDOM_EXPIRE_OFFSET = 300;

    //缓存空值的过期时间
    private static final int EMPTY_CACHE_EXPIRE_SECONDS = 300;

    private final Gson gson = GsonFactory.getGSON();

    /**
     * 插入评论
     */
    public boolean insertComment(Comments comment) {
        boolean result = commentDao.insertComment(comment);
        if (result) {
            RedisUtil.delete(COMMENT_INFO_PREFIX + comment.getCommentId());
            return true;
        } else {
            throw new CommentException("评论插入失败");
        }
    }

    /**
     * 查询单条评论信息
     */
    public Comments selectCommentByCommentId(Integer commentId) {
        if (commentId < 100000) {
            String cacheKey = COMMENT_INFO_PREFIX + commentId;
            String cachedCommentJson = RedisUtil.get(cacheKey);

            Comments comment;
            if (cachedCommentJson != null) {
                if ("NOT_EXISTS".equals(cachedCommentJson)) {
                    throw new CommentException("评论不存在");
                }
                comment = gson.fromJson(cachedCommentJson, Comments.class);
            } else {
                comment = commentDao.selectCommentByCommentId(commentId);
                if (comment == null) {
                    RedisUtil.set(cacheKey, "NOT_EXISTS", EMPTY_CACHE_EXPIRE_SECONDS);
                    throw new CommentException("评论不存在");
                }
                RedisUtil.set(cacheKey, gson.toJson(comment), getRandomExpireTime());
            }
            return comment;
        } else {
            throw new CommentException("搜索评论失败");
        }
    }

    /**
     * 删除评论
     */
    public boolean deleteComment(Integer commentId) {
        boolean result = commentDao.deleteComment(commentId);
        if (result) {
            RedisUtil.delete(COMMENT_INFO_PREFIX + commentId);
            return true;
        } else {
            throw new CommentException("删除评论失败");
        }
    }

    /**
     * 查询某目标的所有评论
     */
    public List<Comments> selectCommentsByTargetId(Integer targetId, String sortMode) {
        if ("likes".equals(sortMode) || "time".equals(sortMode)) {
            String cacheKey = COMMENTS_BY_TARGET_PREFIX + targetId + ":" + sortMode;
            String cachedCommentsJson = RedisUtil.get(cacheKey);

            List<Comments> commentsList;
            if (cachedCommentsJson != null) {
                commentsList = gson.fromJson(cachedCommentsJson, new TypeToken<List<Comments>>() {}.getType());
            } else {
                commentsList = commentDao.selectCommentsByTargetId(targetId, sortMode);
                if (commentsList == null || commentsList.isEmpty()) {
                    RedisUtil.set(cacheKey, "[]", EMPTY_CACHE_EXPIRE_SECONDS);
                    throw new CommentException("该目标暂无评论");
                }

                RedisUtil.set(cacheKey, gson.toJson(commentsList), getRandomExpireTime());
            }

            return commentsList;
        } else {
            throw new CommentException("传入的排序信息有误");
        }
    }

    /**
     * 增加评论点赞数
     */
    @Override
    public boolean incrementCommentLikes(Integer commentId) {
        String cacheKey = COMMENT_INFO_PREFIX + commentId;

        String cachedCommentJson = RedisUtil.get(cacheKey);
        Comments comment;
        if (cachedCommentJson != null) {
            if ("NOT_EXISTS".equals(cachedCommentJson)) {
                throw new CommentException("评论不存在");
            }
            comment = gson.fromJson(cachedCommentJson, Comments.class);
        } else {
            comment = commentDao.selectCommentByCommentId(commentId);
            if (comment == null) {
                throw new CommentException("评论不存在");
            }
        }

        comment.setCommentLikes(comment.getCommentLikes() + 1);

        boolean result = commentDao.incrementCommentLikes(commentId);
        if (result) {
            RedisUtil.set(cacheKey, gson.toJson(comment), getRandomExpireTime());
            return true;
        } else {
            throw new CommentException("更新点赞数失败");
        }
    }

    /**
     * 减少评论点赞数
     */
    @Override
    public boolean lowCommentLikes(Integer commentId) {
        String cacheKey = COMMENT_INFO_PREFIX + commentId;

        String cachedCommentJson = RedisUtil.get(cacheKey);
        Comments comment;
        if (cachedCommentJson != null) {
            if ("NOT_EXISTS".equals(cachedCommentJson)) {
                throw new CommentException("评论不存在");
            }
            comment = gson.fromJson(cachedCommentJson, Comments.class);
        } else {
            comment = commentDao.selectCommentByCommentId(commentId);
            if (comment == null) {
                throw new CommentException("评论不存在");
            }
        }

        if (comment.getCommentLikes() <= 0) {
            throw new CommentException("点赞数不能为负");
        }

        comment.setCommentLikes(comment.getCommentLikes() - 1);

        boolean result = commentDao.lowCommentLikes(commentId);
        if (result) {
            RedisUtil.set(cacheKey, gson.toJson(comment), getRandomExpireTime());
            return true;
        } else {
            throw new CommentException("更新点赞数失败");
        }
    }

    /**
     * 获取随机化的缓存过期时间
     */
    private int getRandomExpireTime() {
        return BASE_CACHE_EXPIRE_SECONDS + new Random().nextInt(RANDOM_EXPIRE_OFFSET);
    }
}