package com.peitianbao.www.controller;

import com.peitianbao.www.api.BlogService;
import com.peitianbao.www.api.CommentService;
import com.peitianbao.www.api.ShopService;
import com.peitianbao.www.exception.LikeException;
import com.peitianbao.www.model.Likes;
import com.peitianbao.www.service.LikeService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.ResponseUtil;
import io.seata.core.context.RootContext;
import io.seata.tm.api.GlobalTransaction;
import io.seata.tm.api.GlobalTransactionContext;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class LikeController {
    @Autowired
    private LikeService likeService;

    @DubboReference(serviceName = "shopService")
    private ShopService shopService;

    @DubboReference(serviceName = "commentService")
    private CommentService commentService;

    @DubboReference(serviceName = "blogService")
    private BlogService blogService;

    /**
     * 插入评论点赞
     */
    @RequestMapping(value = "/likeComment", methodType = RequestMethod.POST)
    public void likeComment(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getTargetId();
        Integer likerId = like.getLikerId();

        if (targetId == null || likerId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        try {
            tx.begin(60000, "my_test_tx_group");

            LoggingFramework.info("开始事务：" + tx.getXid());

            boolean result1 = likeService.commentLike(targetId, likerId);
            boolean result2 = commentService.incrementCommentLikes(targetId);

            if (!result1 || !result2) {
                LoggingFramework.warning("点赞失败，准备回滚事务");
                tx.rollback();
                throw new LikeException("[400] 点赞失败");
            }

            LoggingFramework.info("点赞成功，提交事务");
            tx.commit();

            Map<String, Object> responseData = Map.of("message", "评论成功点赞");
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("发生异常，强制回滚事务: " + e.getMessage());
            try {
                if (tx != null && RootContext.inGlobalTransaction()) {
                    tx.rollback();
                    LoggingFramework.info("事务已手动回滚");
                }
            } catch (Exception rollbackEx) {
                LoggingFramework.severe("事务回滚失败: " + rollbackEx.getMessage());
            }
            throw new LikeException("[500] 点赞异常");
        }
    }

    /**
     * 插入商铺点赞
     */
    @RequestMapping(value = "/likeShop", methodType = RequestMethod.POST)
    public void likeShop(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getTargetId();
        Integer likerId = like.getLikerId();

        if (targetId == null || likerId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        try {
            tx.begin(60000, "my_test_tx_group");
            LoggingFramework.info("开始商铺点赞事务：" + tx.getXid());

            boolean result1 = likeService.shopLike(targetId, likerId);
            boolean result2 = shopService.incrementShopLikes(targetId);

            if (!result1 || !result2) {
                LoggingFramework.warning("商铺点赞失败，准备回滚事务");
                tx.rollback();
                throw new LikeException("[400] 点赞失败");
            }

            LoggingFramework.info("商铺点赞成功，提交事务");
            tx.commit();

            Map<String, Object> responseData = Map.of("message", "商铺成功点赞");
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("发生异常，强制回滚事务: " + e.getMessage());
            try {
                if (tx != null && RootContext.inGlobalTransaction()) {
                    tx.rollback();
                    LoggingFramework.info("商铺点赞事务已手动回滚");
                }
            } catch (Exception rollbackEx) {
                LoggingFramework.severe("事务回滚失败: " + rollbackEx.getMessage());
            }

            throw new LikeException("[500] 商铺点赞异常：" + e.getMessage());
        }
    }

    /**
     * 插入动态点赞
     */
    @RequestMapping(value = "/likeBlog", methodType = RequestMethod.POST)
    public void likeBlog(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getTargetId();
        Integer likerId = like.getLikerId();

        if (targetId == null || likerId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        GlobalTransaction tx = GlobalTransactionContext.getCurrentOrCreate();

        try {
            tx.begin(60000, "my_test_tx_group");
            LoggingFramework.info("开始动态点赞事务：" + tx.getXid());

            boolean result1 = likeService.blogLike(targetId, likerId);
            boolean result2 = blogService.incrementBlogLikes(targetId);

            if (!result1 || !result2) {
                LoggingFramework.warning("动态点赞失败，准备回滚事务");
                tx.rollback();
                throw new LikeException("[400] 点赞失败");
            }

            LoggingFramework.info("动态点赞成功，提交事务");
            tx.commit();

            Map<String, Object> responseData = Map.of("message", "动态成功点赞");
            ResponseUtil.sendSuccessResponse(resp, responseData);

        } catch (Exception e) {
            LoggingFramework.severe("发生异常，强制回滚事务: " + e.getMessage());
            try {
                if (tx != null && RootContext.inGlobalTransaction()) {
                    tx.rollback();
                    LoggingFramework.info("动态点赞事务已手动回滚");
                }
            } catch (Exception rollbackEx) {
                LoggingFramework.severe("事务回滚失败: " + rollbackEx.getMessage());
            }

            throw new LikeException("[500] 动态点赞异常：" + e.getMessage());
        }
    }

    /**
     * 查询商铺点赞列表
     */
    @RequestMapping(value = "/likeShopList", methodType = RequestMethod.POST)
    public void likeShopList(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getTargetId();

        if(targetId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        List<Likes> result = likeService.selectShopLikes(targetId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "商铺点赞列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 商铺点赞列表查询失败");
        }
    }

    /**
     * 查询动态点赞列表
     */
    @RequestMapping(value = "/likeBlogList", methodType = RequestMethod.POST)
    public void likeBlogList(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getTargetId();

        if(targetId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        List<Likes> result = likeService.selectBlogLikes(targetId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "动态点赞列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 动态点赞列表查询失败");
        }
    }

    /**
     * 查询评论点赞列表
     */
    @RequestMapping(value = "/likeCommentList", methodType = RequestMethod.POST)
    public void likeCommentList(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getTargetId();

        if(targetId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        List<Likes> result = likeService.selectCommentLikes(targetId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "评论点赞列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 评论点赞列表查询失败");
        }
    }

    /**
     * 查询用户点赞商铺列表
     */
    @RequestMapping(value = "/userLikesShopsList", methodType = RequestMethod.POST)
    public void userLikesShopsList(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getLikerId();

        if(targetId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        List<Likes> result = likeService.selectUserLikesShops(targetId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "用户点赞商铺列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 用户点赞商铺列表查询失败");
        }
    }

    /**
     * 查询用户点赞评论列表
     */
    @RequestMapping(value = "/userLikesCommentsList", methodType = RequestMethod.POST)
    public void userLikesCommentsList(@MyRequestBody Likes like, HttpServletResponse resp) throws IOException {
        Integer targetId = like.getLikerId();

        if(targetId == null) {
            throw new LikeException("[401] 请求参数有误");
        }

        List<Likes> result = likeService.selectUserLikesComments(targetId);

        if(result!=null) {
            Map<String, Object> responseData = Map.of(
                    "message", "用户点赞评论列表查询成功",
                    "data", result
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new LikeException("[400] 用户点赞评论列表查询失败");
        }
    }
}
