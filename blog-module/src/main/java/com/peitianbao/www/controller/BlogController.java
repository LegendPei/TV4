package com.peitianbao.www.controller;

import com.peitianbao.www.api.FollowService;
import com.peitianbao.www.exception.BlogException;
import com.peitianbao.www.model.BlogCollection;
import com.peitianbao.www.model.Blogs;
import com.peitianbao.www.model.BlogsSort;
import com.peitianbao.www.service.BlogService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.ResponseUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class BlogController {
    @Autowired
    private BlogService blogService;

    @DubboReference(serviceName = "followService")
    private FollowService followService;

    /**
     * 发表动态
     */
    @RequestMapping(value = "/createBlog",methodType = RequestMethod.POST)
    public void createBlog(@MyRequestBody Blogs blog, HttpServletResponse resp)throws IOException{
        Integer targetId= blog.getTargetId();
        String blogName = blog.getBlogName();
        Integer authorId = blog.getAuthorId();
        String blogContent = blog.getBlogContent();
        String filePath = blog.getFilePath();
        int blogType = blog.getBlogType();
        if(blogName==null||authorId==null||blogContent==null){
            throw new BlogException("[401] 传入信息有误");
        }

        Blogs blogs=new Blogs(targetId,blogName,authorId,blogContent,filePath,blogType);
        boolean result = blogService.createBlog(blogs);
        if(result){
            Map<String, Object> responseData = Map.of(
                    "message", "创建动态成功"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new BlogException("[400] 创建动态失败");
        }
    }

    /**
     * 查询动态信息
     */
    @RequestMapping(value = "/getBlogInfo",methodType = RequestMethod.POST)
    public void getBlogInfo(@MyRequestBody Blogs blog, HttpServletResponse resp)throws IOException{
        Integer blogId = blog.getBlogId();
        if(blogId<400000||blogId>500000){
            throw new BlogException("[401] 请求动态的信息有误");
        }
        Blogs blogs = blogService.getBlogInfo(blogId);
        Map<String, Object> responseData = Map.of(
                "targetId", blogs.getTargetId(),
                "blogName", blogs.getBlogName(),
                "authorId", blogs.getAuthorId(),
                "blogContent", blogs.getBlogContent(),
                "blogTime", blogs.getBlogTime(),
                "blogLikes", blogs.getBlogLikes(),
                "blogCollections", blogs.getBlogCollections(),
                "filePath", blogs.getFilePath(),
                "blogType", blogs.getBlogType()
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 获取用户的动态列表
     */
    @RequestMapping(value = "/getUserBlogs",methodType = RequestMethod.POST)
    public void getUserBlogs(@MyRequestBody BlogsSort blog, HttpServletResponse resp)throws IOException{
        Integer userId = blog.getBlog().getAuthorId();
        String sortMode=blog.getSortMode().getSortType();
        if(userId<100000||userId>=200000){
            throw new BlogException("[401] 请求参数有误");
        }
        List<Blogs> blogs = blogService.getUserBlogs(userId,sortMode);
        Map<String, Object> responseData = Map.of(
                "message", "用户的动态列表查询成功",
                "data", blogs
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 获取商铺的动态列表
     */
    @RequestMapping(value = "/getShopBlogs",methodType = RequestMethod.POST)
    public void getShopBlogs(@MyRequestBody BlogsSort blog, HttpServletResponse resp)throws IOException{
        Integer shopId = blog.getBlog().getAuthorId();
        String sortMode=blog.getSortMode().getSortType();
        if(shopId<200000||shopId>=300000){
            throw new BlogException("[401] 请求参数有误");
        }
        List<Blogs> blogs = blogService.getShopBlogs(shopId,sortMode);
        Map<String, Object> responseData = Map.of(
                "message", "商铺的动态列表查询成功",
                "data", blogs
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 获取用户收藏的动态列表
     */
    @RequestMapping(value = "/getUserCollectBlogs",methodType = RequestMethod.POST)
    public void getUserCollectBlogs(@MyRequestBody BlogCollection blog, HttpServletResponse resp)throws IOException{
        Integer userId = blog.getUserId();
        if(userId<100000||userId>=200000){
            throw new BlogException("[401] 用户id有误");
        }
        List<BlogCollection>blogCollections = blogService.getUserCollectBlogs(userId);
        Map<String, Object> responseData = Map.of(
                "message", "用户收藏的动态列表查询成功",
                "data", blogCollections
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }

    /**
     * 用户收藏动态
     */
    @RequestMapping(value = "/collectBlog",methodType = RequestMethod.POST)
    public void collectBlog(@MyRequestBody BlogCollection blog, HttpServletResponse resp)throws IOException{
        Integer blogId = blog.getBlogId();
        Integer userId = blog.getUserId();
        if(blogId<400000){
            throw new BlogException("[401] 输入的动态id有误");
        }
        boolean result = blogService.collectBlog(blogId,userId);
        if(result){
            Map<String, Object> responseData = Map.of(
                    "message", "用户收藏动态成功"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new BlogException("[400] 收藏失败");
        }
    }

    /**
     * 用户取消收藏动态
     */
    @RequestMapping(value = "/unCollectBlog",methodType = RequestMethod.POST)
    public void unCollectBlog(@MyRequestBody BlogCollection blog, HttpServletResponse resp)throws IOException{
        Integer blogId = blog.getBlogId();
        Integer userId = blog.getUserId();
        if(blogId<400000){
            throw new BlogException("[401] 输入的动态id有误");
        }
        boolean result = blogService.unCollectBlog(blogId,userId);
        if(result){
            Map<String, Object> responseData = Map.of(
                    "message", "用户取消收藏动态成功"
            );
            ResponseUtil.sendSuccessResponse(resp, responseData);
        }else {
            throw new BlogException("[400] 取消收藏失败");
        }
    }

    /**
     * 用户上线后拉取关注的人和商铺的动态
     */
    @RequestMapping(value = "/getTimeline", methodType = RequestMethod.POST)
    public void getTimeline(@MyRequestBody BlogCollection user, HttpServletResponse resp) throws IOException {
        Integer userId = user.getUserId();
        if (userId == null || userId < 100000) {
            throw new BlogException("[401] 用户ID错误");
        }

        List<Blogs> timeline = new ArrayList<>();

        //获取用户关注的用户ID列表
        List<Integer> followedUsers = followService.followingUsers(userId);
        if (followedUsers != null && !followedUsers.isEmpty()) {
            for (Integer id : followedUsers) {
                List<Blogs> blogs = blogService.getUserBlogs(id, "time");
                if (blogs != null && !blogs.isEmpty()) {
                    timeline.addAll(blogs);
                }
            }
        }

        //获取用户关注的商铺ID列表
        List<Integer> followedShops = followService.followingShops(userId);
        if (followedShops != null && !followedShops.isEmpty()) {
            for (Integer shopId : followedShops) {
                List<Blogs> blogs = blogService.getShopBlogs(shopId, "time");
                if (blogs != null && !blogs.isEmpty()) {
                    timeline.addAll(blogs);
                }
            }
        }

        //合并并按时间排序（从新到旧）
        timeline.sort((b1, b2) -> b2.getBlogTime().compareTo(b1.getBlogTime()));

        //返回给前端
        Map<String, Object> responseData = Map.of(
                "message", "拉取成功",
                "data", timeline
        );
        ResponseUtil.sendSuccessResponse(resp, responseData);
    }
}
