package com.peitianbao.www.api;

/**
 * @author leg
 */
public interface BlogService {
    /**
     * 增加动态点赞
     * @param id 动态id
     * @return 是否增加成功
     */
    boolean incrementBlogLikes(Integer id);

    /**
     * 减少动态点赞
     * @param id 动态id
     * @return 是否减少成功
     */
    boolean lowBlogLikes(Integer id);
}
