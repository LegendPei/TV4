package com.peitianbao.www.dao;


import com.peitianbao.www.exception.CommentException;
import com.peitianbao.www.model.Comments;
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
public class CommentDaoImpl implements CommentDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    public boolean insertComment(Comments comment) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("commenterId", comment.getCommenterId());
            params.put("targetId", comment.getTargetId());
            params.put("commentContent", comment.getCommentContent());

            LoggingFramework.info("尝试插入评论：" + comment.getCommentContent());
            int result = sqlSession.executeUpdate("CommentMapper.insertComment", params);
            LoggingFramework.info("评论插入成功：" + comment.getCommentContent());
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入评论失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new CommentException("插入评论失败", e);
        }
    }

    @Override
    public Comments selectCommentByCommentId(Integer commentId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("commentId", commentId);

            LoggingFramework.info("尝试查询评论 ID：" + commentId);
            Comments comment = sqlSession.executeQueryForObject("CommentMapper.selectCommentById", params, Comments.class);

            if (comment != null) {
                LoggingFramework.info("查询到评论：" + comment.getCommentContent());
            } else {
                LoggingFramework.warning("未找到评论 ID：" + commentId);
            }
            return comment;
        } catch (Exception e) {
            LoggingFramework.severe("查询评论失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public boolean deleteComment(Integer commentId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("commentId", commentId);

            LoggingFramework.info("尝试删除评论 ID：" + commentId);
            int result = sqlSession.executeUpdate("CommentMapper.deleteCommentById", params);
            LoggingFramework.info("评论删除成功：" + commentId);
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("删除评论失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new CommentException("删除评论失败", e);
        }
    }

    @Override
    public List<Comments> selectCommentsByCommenterId(Integer commenterId, String sortMode) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("commenterId", commenterId);

            LoggingFramework.info("尝试查询评论人 ID：" + commenterId);

            List<Comments> comments = sqlSession.executeQueryForList("CommentMapper.selectCommentsByCommenterId", params, Comments.class);

            if (comments != null && !comments.isEmpty()) {
                LoggingFramework.info("查询到 " + comments.size() + " 条评论");
            } else {
                LoggingFramework.warning("未找到评论人 ID：" + commenterId);
            }

            return comments;
        } catch (Exception e) {
            LoggingFramework.severe("查询评论失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Comments> selectCommentsByTargetId(Integer targetId, String sortMode) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", targetId);

            LoggingFramework.info("尝试查询被评论对象 ID：" + targetId);

            List<Comments> comments = sqlSession.executeQueryForList("CommentMapper.selectCommentsByTargetId", params, Comments.class);

            if (comments != null && !comments.isEmpty()) {
                LoggingFramework.info("查询到 " + comments.size() + " 条评论");
            } else {
                LoggingFramework.warning("未找到被评论对象 ID：" + targetId);
            }
            return comments;
        } catch (Exception e) {
            LoggingFramework.severe("查询评论失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    private List<Comments> sortComments(List<Comments> comments, String sortMode) {
        if ("time".equalsIgnoreCase(sortMode)) {
            //按时间排序（降序）
            comments.sort(Comparator.comparing(Comments::getCommentTime).reversed());
        } else if ("likes".equalsIgnoreCase(sortMode)) {
            //按点赞数排序（降序），点赞数相同则按时间排序（降序）
            comments.sort(Comparator.comparing(Comments::getCommentLikes)
                    .thenComparing(Comments::getCommentTime).reversed());
        } else {
            throw new IllegalArgumentException("无效的排序模式: " + sortMode);
        }
        return comments;
    }
}
