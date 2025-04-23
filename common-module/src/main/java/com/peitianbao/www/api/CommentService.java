package com.peitianbao.www.api;

/**
 * @author leg
 */
public interface CommentService {
    /**
     * 增加评论点赞数
     * @param commentId 评论id
     * @return 是否成功
     */
    public boolean incrementCommentLikes(Integer commentId);

    /**
     * 减少评论点赞数
     * @param commentId 评论id
     * @return 是否成功
     */
    public boolean lowCommentLikes(Integer commentId);
}
