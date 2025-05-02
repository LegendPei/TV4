package com.peitianbao.www.dao;

import com.peitianbao.www.exception.LikeException;
import com.peitianbao.www.model.Likes;
import com.peitianbao.www.springframework.annontion.Autowired;
import com.peitianbao.www.springframework.annontion.Dao;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Dao
public class LikeDaoImpl implements LikeDao {
    @Autowired
    private SqlSession sqlSession;

    @Override
    public boolean insertShopLike(Likes likes) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", likes.getTargetId());
            params.put("likerId", likes.getLikerId());

            LoggingFramework.info("尝试插入商铺点赞记录：" + likes);
            int result = sqlSession.executeUpdate("ShopsLikesMapper.insertShopsLike", params);
            LoggingFramework.info("商铺点赞记录插入成功：" + likes);
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入商铺点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new LikeException("插入商铺点赞记录失败", e);
        }
    }

    @Override
    public boolean insertCommentLike(Likes commentsLike) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", commentsLike.getTargetId());
            params.put("likerId", commentsLike.getLikerId());

            LoggingFramework.info("尝试插入点赞记录：" + commentsLike);
            int result = sqlSession.executeUpdate("CommentsLikesMapper.insertCommentsLike", params);
            LoggingFramework.info("点赞记录插入成功：" + commentsLike);
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new LikeException("插入点赞记录失败", e);
        }
    }

    @Override
    public boolean insertBlogLike(Likes likes) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", likes.getTargetId());
            params.put("likerId", likes.getLikerId());

            LoggingFramework.info("尝试插入动态点赞记录：" + likes);
            int result = sqlSession.executeUpdate("BlogsLikesMapper.insertBlogsLike", params);
            LoggingFramework.info("动态点赞记录插入成功：" + likes);
            return result!=0;
        } catch (Exception e) {
            LoggingFramework.severe("插入动态点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            throw new LikeException("插入动态点赞记录失败", e);
        }
    }

    @Override
    public List<Likes> selectShopLikes(Integer shopId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", shopId);

            LoggingFramework.info("尝试查询商铺 ID：" + shopId + " 的点赞记录");
            return sqlSession.executeQueryForList("ShopsLikesMapper.selectShopsLikesByShopId", params, Likes.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询商铺点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Likes> selectBlogLikes(Integer blogId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", blogId);

            LoggingFramework.info("尝试查询动态 ID：" + blogId + " 的点赞记录");
            return sqlSession.executeQueryForList("BlogsLikesMapper.selectBlogsLikesByBlogId", params, Likes.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询动态点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Likes> selectCommentLikes(Integer commentId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("targetId", commentId);

            LoggingFramework.info("尝试查询评论 ID：" + commentId + " 的点赞记录");
            return sqlSession.executeQueryForList("CommentsLikesMapper.selectCommentsLikesByCommentId", params, Likes.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Likes> selectUserLikesShops(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("likerId", userId);

            LoggingFramework.info("尝试查询点赞人 ID：" + userId + " 的商铺点赞记录");
            return sqlSession.executeQueryForList("ShopsLikesMapper.selectShopsLikesByShopLikeId", params, Likes.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询商铺点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }

    @Override
    public List<Likes> selectUserLikesComments(Integer userId) {
        try {
            Map<String, Object> params = new HashMap<>();
            params.put("likerId", userId);

            LoggingFramework.info("尝试查询点赞人 ID：" + userId + " 的点赞记录");
            return sqlSession.executeQueryForList("CommentsLikesMapper.selectCommentsLikesByCommentLikeId", params, Likes.class);
        } catch (Exception e) {
            LoggingFramework.severe("查询点赞记录失败：" + e.getMessage());
            LoggingFramework.logException(e);
            return null;
        }
    }
}
