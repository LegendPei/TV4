package com.peitianbao.www.controller;

import com.peitianbao.www.exception.BlogException;
import com.peitianbao.www.model.Blogs;
import com.peitianbao.www.service.BlogService;
import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.util.ResponseUtil;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * @author leg
 */
@Controller
public class BlogController {
    @Autowired
    private BlogService blogService;

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


    }

}
