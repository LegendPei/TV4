package com.peitianbao.www.controller;

import com.peitianbao.www.exception.CommentException;
import com.peitianbao.www.model.Comments;
import com.peitianbao.www.model.FindAllUserCommentsRequest;
import com.peitianbao.www.model.SortRequest;
import com.peitianbao.www.service.CommentService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.ResponseUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class CommentController {
    @Autowired
    CommentService commentService;

    /**
     * 发表评论
     */
    @RequestMapping(value="/addComment",methodType = RequestMethod.POST)
    public void addComment(@MyRequestBody Comments comments, HttpServletResponse resp)throws IOException {
        String commentContent = comments.getCommentContent();
        Integer commenterId = comments.getCommenterId();
        Integer targetId = comments.getTargetId();

        if(commentContent==null){
            throw new CommentException("[400] 评论内容不能为空");
        }

        Comments comment = new Comments(targetId, commenterId, commentContent);
        boolean result = commentService.insertComment(comment);

        if(result){
            Map<String, Object> responseData = Map.of(
                    "message", "用户成功评论"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new CommentException("[401] 评论失败");
        }
    }

    /**
     * 查询单条评论信息
     */
    @RequestMapping(value = "/commentInfo",methodType = RequestMethod.POST)
    public void commentInfo(@MyRequestBody Comments comments, HttpServletResponse resp)throws IOException {
        Integer commentId = comments.getCommentId();

        if(commentId==null){
            throw new CommentException("[400] 评论id为空");
        }

        Comments comment = commentService.selectCommentByCommentId(commentId);
        Integer targetId = comment.getTargetId();
        Integer commenterId = comment.getCommenterId();
        String commentContent = comment.getCommentContent();
        int commentLikes = comment.getCommentLikes();
        LocalDateTime commentTime = comment.getCommentTime();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formattedTime = commentTime.format(formatter);

        Map<String, Object> responseData = Map.of(
                "message", "成功查询评论",
                "commenterId", commenterId,
                "targetId", targetId,
                "commentContent", commentContent,
                "commentTime", formattedTime,
                "commentLikes", commentLikes
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 删除某条评论
     */
    @RequestMapping(value="/deleteComment",methodType = RequestMethod.DELETE)
    public void deleteComment(@MyRequestBody Comments comments, HttpServletResponse resp)throws IOException {
        Integer commentId = comments.getCommentId();
        if(commentId==null){
            throw new CommentException("[401] 不能删除不存在的评论");
        }

        boolean result = commentService.deleteComment(commentId);
        if(result){
            Map<String, Object> responseData = Map.of(
                    "message", "删除成功"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else{
            throw new CommentException("[404] 删除失败");
        }
    }

    /**
     * 查询某用户的所有评论
     */
    @RequestMapping(value="/findAllUserComments",methodType = RequestMethod.POST)
    public void findAllUserComments(@MyRequestBody FindAllUserCommentsRequest request, HttpServletResponse resp)throws IOException {
        Comments comment = request.getComments();
        SortRequest sortRequest = request.getSortRequest();

        Integer commentedId = comment.getCommenterId();
        String sortMode = sortRequest.getSortType();
        if(commentedId==null){
            throw new CommentException("[401] 传入的用户信息错误");
        }

        List<Comments> comments=commentService.selectCommentsByTargetId(commentedId,sortMode);

        Map<String, Object> responseData = Map.of(
                "message", "用户评论查询成功",
                "data", comments
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 查询某目标的所有评论
     */
    @RequestMapping(value="/findAllShopComments",methodType = RequestMethod.POST)
    public void findAllShopComments(@MyRequestBody FindAllUserCommentsRequest request, HttpServletResponse resp)throws IOException {
        Comments comment = request.getComments();
        SortRequest sortRequest = request.getSortRequest();

        Integer targetId = comment.getTargetId();
        String sortMode = sortRequest.getSortType();
        if(targetId==null){
            throw new CommentException("[401] 传入的商铺信息错误");
        }

        List<Comments> comments=commentService.selectCommentsByTargetId(targetId,sortMode);

        Map<String, Object> responseData = Map.of(
                "message", "商铺评论查询成功",
                "data", comments
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }
}
