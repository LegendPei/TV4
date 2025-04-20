package com.peitianbao.www.service;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.peitianbao.www.dao.CommentDao;
import com.peitianbao.www.exception.CommentException;
import com.peitianbao.www.model.Comments;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Service;
import com.peitianbao.www.util.token.RedisUtil;

import java.util.List;

/**
 * @author leg
 */
@Service
public class CommentService {

    @Autowired
    private CommentDao commentDao;

    // 缓存前缀
    private static final String COMMENT_INFO_PREFIX = "comment:info:";
    private static final String COMMENTS_BY_TARGET_PREFIX = "comments:byTarget:";

    // 缓存过期时间（单位：秒）
    private static final int CACHE_EXPIRE_SECONDS = 3600;

    /**
     * 插入评论
     */
    public boolean insertComment(Comments comment) {
        boolean result = commentDao.insertComment(comment);
        if (result) {
            //清除可能存在的缓存
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
                //从缓存中获取评论信息
                comment = new Gson().fromJson(cachedCommentJson, Comments.class);
            } else {
                //从数据库中加载评论信息
                comment = commentDao.selectCommentByCommentId(commentId);
                if (comment != null) {
                    //写入缓存
                    RedisUtil.set(cacheKey, new Gson().toJson(comment), CACHE_EXPIRE_SECONDS);
                }
            }

            if (comment == null) {
                throw new CommentException("评论不存在");
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
            // 清除缓存
            RedisUtil.delete(COMMENT_INFO_PREFIX + commentId);
            return true;
        } else {
            throw new CommentException("删除评论失败");
        }
    }

    /**
     * 查询某用户的所有评论
     */
    public List<Comments> selectCommentsByCommenterId(Integer commenterId, String sortMode) {
        if ("likes".equals(sortMode) || "time".equals(sortMode)) {
            return commentDao.selectCommentsByCommenterId(commenterId, sortMode);
        } else {
            throw new CommentException("传入的排序信息有误");
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
                // 从缓存中获取评论列表
                commentsList = new Gson().fromJson(cachedCommentsJson, new TypeToken<List<Comments>>() {}.getType());
            } else {
                // 从数据库中加载评论列表
                commentsList = commentDao.selectCommentsByTargetId(targetId, sortMode);
                if (commentsList != null) {
                    // 写入缓存
                    RedisUtil.set(cacheKey, new Gson().toJson(commentsList), CACHE_EXPIRE_SECONDS);
                }
            }

            if (commentsList == null || commentsList.isEmpty()) {
                throw new CommentException("该目标暂无评论");
            }
            return commentsList;
        } else {
            throw new CommentException("传入的排序信息有误");
        }
    }
}