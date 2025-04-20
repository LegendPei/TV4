package com.peitianbao.www.dao;

import com.peitianbao.www.model.Comments;

import java.util.List;

/**
 * @author leg
 */
public interface CommentDao {
    /**
     * 插入评论
     * @param comment 评论信息
     * @return 是否插入成功
     */
    boolean insertComment(Comments comment);

    /**
     * 通过评论id找到评论
     * @param commentId 评论id
     * @return 评论信息
     */
    Comments selectCommentByCommentId(Integer commentId);

    /**
     * 删除评论
     * @param commentId 评论id
     * @return 是否成功删除
     */
    boolean deleteComment(Integer commentId);

    /**
     * 同过评论者id找到其的评论，并排序
     * @param commenterId 评论者id
     * @param sortMode 排序模式
     * @return 排序后的评论集合
     */
    List<Comments> selectCommentsByCommenterId(Integer commenterId,String sortMode);

    /**
     * 同过评论目标id找到其的评论，并排序
     * @param targetId 目标id
     * @param sortMode 排序模式
     * @return 排序后的评论集合
     */
    List<Comments> selectCommentsByTargetId(Integer targetId,String sortMode);
}
