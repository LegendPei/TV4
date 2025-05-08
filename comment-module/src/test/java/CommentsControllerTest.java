import com.peitianbao.www.controller.CommentController;
import com.peitianbao.www.exception.CommentException;
import com.peitianbao.www.model.Comments;
import com.peitianbao.www.service.CommentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.LocalDateTime;

class CommentsControllerTest {

    private CommentService mockService;
    private CommentController controller;
    private HttpServletResponse response;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() {
        mockService = mock(CommentService.class);
        controller = new CommentController();

        // 设置模拟响应对象
        response = mock(HttpServletResponse.class);
        stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        try {
            when(response.getWriter()).thenReturn(writer);
        } catch (IOException e) {
            fail("初始化响应失败：" + e.getMessage());
        }
    }

    /**
     * 测试 /addComment 成功情况
     */
    @Test
    void testAddComment_Success() throws Exception {
        // 构造输入数据
        Comments input = new Comments(200013, 100007, "这是一条测试评论");

        // 设置 service 返回 true
        when(mockService.insertComment(any(Comments.class))).thenReturn(true);

        // 调用方法
        controller.addComment(input, response);

        // 获取输出结果
        String result = stringWriter.toString().trim();

        // 验证结果
        assertTrue(result.contains("\"message\":\"用户成功评论\""));
    }

    /**
     * 测试 /addComment 空内容 → 抛出异常
     */
    @Test
    void testAddComment_EmptyContent() {
        // 构造非法输入
        Comments input = new Comments();

        // 应该抛出异常
        assertThrows(CommentException.class, () -> controller.addComment(input, response));
    }

    /**
     * 测试 /commentInfo 成功返回
     */
    @Test
    void testCommentInfo_Success() throws Exception {
        // 构造评论数据
        Comments mockComment = new Comments(200013, 100007, "这是我的评论");
        mockComment.setCommentId(500001);
        mockComment.setCommentLikes(6);
        mockComment.setCommentTime(LocalDateTime.now());

        // 设置 service 返回 mock 数据
        when(mockService.selectCommentByCommentId(500001)).thenReturn(mockComment);

        // 构造请求数据
        Comments input = new Comments();
        input.setCommentId(500001);

        // 执行 controller 方法
        controller.commentInfo(input, response);

        // 获取输出结果
        String result = stringWriter.toString().trim();

        // 验证结果
        assertTrue(result.contains("\"message\":\"成功查询评论\""));
        assertTrue(result.contains("\"commenterId\":100007"));
        assertTrue(result.contains("\"targetId\":200013"));
        assertTrue(result.contains("\"commentContent\":\"这是我的评论\""));
        assertTrue(result.contains("\"commentLikes\":6"));
    }

    /**
     * 测试 /commentInfo 缺少 commentId → 抛出异常
     */
    @Test
    void testCommentInfo_MissingId() {
        // 构造空 ID 的输入
        Comments input = new Comments();
        input.setCommentId(null);

        // 应该抛出异常
        assertThrows(CommentException.class, () -> controller.commentInfo(input, response));
    }
}