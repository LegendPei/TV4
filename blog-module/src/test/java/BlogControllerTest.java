import com.peitianbao.www.controller.BlogController;
import com.peitianbao.www.exception.BlogException;
import com.peitianbao.www.model.Blogs;
import com.peitianbao.www.service.BlogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;

class BlogControllerTest {

    private BlogService blogService;
    private BlogController blogController;
    private HttpServletResponse response;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() {
        blogService = mock(BlogService.class);
        blogController = new BlogController();
        response = mock(HttpServletResponse.class);
        stringWriter = new StringWriter();

        // 模拟 PrintWriter
        try {
            when(response.getWriter()).thenReturn(new PrintWriter(stringWriter));
        } catch (IOException e) {
            fail("模拟响应失败：" + e.getMessage());
        }
    }

    /**
     * 测试 /createBlog 接口
     */
    @Test
    void testCreateBlog_Success() throws Exception {
        // 构造参数
        Blogs blog = new Blogs(200013, "我的动态", 100007, "这是内容", "http://localhost/upload/xxx.jpg", 1);

        // 设置 service 返回 true
        when(blogService.createBlog(any(Blogs.class))).thenReturn(true);

        // 调用 controller 方法
        blogController.createBlog(blog, response);

        // 验证输出
        String result = stringWriter.toString().trim();
        assertTrue(result.contains("\"message\":\"创建动态成功\""));
    }

    @Test
    void testCreateBlog_Failure() {
        // 构造非法参数
        Blogs blog = new Blogs(null, "标题", null, "内容", "0", 1);

        // 设置 service 返回 false
        when(blogService.createBlog(any(Blogs.class))).thenReturn(false);

        // 应该抛出 BlogException
        assertThrows(BlogException.class, () -> blogController.createBlog(blog, response));
    }

    @Test
    void testCreateBlog_InvalidInput() throws Exception {
        // 构造非法 blog（缺少必要字段）
        Blogs blog = new Blogs(null, null, null, null, null, 1);

        // 不调用 service
        blogController.createBlog(blog, response);

        // 验证是否抛出异常
        assertThrows(BlogException.class, () -> blogController.createBlog(blog, response));
    }

    /**
     * 测试 /getBlogInfo 接口
     */
    @Test
    void testGetBlogInfo_ValidId() throws Exception {
        // 构造 blogId 在 400000 - 500000 之间
        Blogs input = new Blogs();
        input.setBlogId(400003);

        // 构造返回数据
        Blogs output = new Blogs(200013, "商铺动态", 100007, "动态内容", "http://TV/upload/abc.png", 2);
        when(blogService.getBlogInfo(400003)).thenReturn(output);

        // 调用接口
        blogController.getBlogInfo(input, response);

        // 验证响应中包含所有字段
        String result = stringWriter.toString().trim();
        assertTrue(result.contains("\"targetId\":200013"));
        assertTrue(result.contains("\"blogName\":\"商铺动态\""));
        assertTrue(result.contains("\"authorId\":100007"));
        assertTrue(result.contains("\"blogContent\":\"动态内容\""));
        assertTrue(result.contains("\"filePath\":\"http://TV/upload/abc.png\""));
        assertTrue(result.contains("\"blogType\":2"));
    }

    @Test
    void testGetBlogInfo_InvalidId()  {
        // 构造非法 blogId
        Blogs blog = new Blogs();
        blog.setBlogId(399999); // 小于 400000

        assertThrows(BlogException.class, () -> blogController.getBlogInfo(blog, response));
    }
}