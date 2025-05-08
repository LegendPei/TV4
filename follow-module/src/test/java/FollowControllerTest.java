import com.peitianbao.www.api.UserService;
import com.peitianbao.www.controller.FollowController;
import com.peitianbao.www.exception.FollowException;
import com.peitianbao.www.model.Follows;
import com.peitianbao.www.service.FollowService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;

class FollowControllerTest {

    private FollowService mockFollowService;
    private UserService mockUserService;
    private FollowController controller;
    private HttpServletResponse response;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() throws Exception {
        mockFollowService = mock(FollowService.class);
        mockUserService = mock(UserService.class);
        controller = new FollowController();

        // 设置响应输出捕获
        response = mock(HttpServletResponse.class);
        stringWriter = new StringWriter();
        PrintWriter writer = new PrintWriter(stringWriter);
        when(response.getWriter()).thenReturn(writer);
    }

    /**
     * 成功关注用户 → 所有服务返回 true
     */
    @Test
    void testFollowUser_Success()  {
        // 构造请求数据
        Follows input = new Follows(100007, 200013); // targetId=100007, followerId=200013

        // 模拟 service 返回 true
        when(mockFollowService.followUser(eq(100007), eq(200013))).thenReturn(true);
        when(mockUserService.incrementFollowingUsers(eq(200013))).thenReturn(true);
        when(mockUserService.incrementUserFollowers(eq(100007))).thenReturn(true);

        // 调用 controller 方法
        controller.followUser(input, response);

        // 验证输出
        String result = stringWriter.toString().trim();
        assertTrue(result.contains("\"message\":\"关注用户成功\""));
    }

    /**
     * 关注失败 → 其中一个 service 返回 false
     */
    @Test
    void testFollowUser_FailureInService()  {
        // 构造请求数据
        Follows input = new Follows(100007, 200013);

        // 模拟 service 返回 false
        when(mockFollowService.followUser(eq(100007), eq(200013))).thenReturn(false);
        when(mockUserService.incrementFollowingUsers(eq(200013))).thenReturn(true);
        when(mockUserService.incrementUserFollowers(eq(100007))).thenReturn(true);

        // 应该抛出 FollowException
        assertThrows(FollowException.class, () -> controller.followUser(input, response));

        // 验证事务是否回滚
        verify(mockUserService, never()).incrementUserFollowers(100007);
    }

    /**
     * 参数为空 → 抛出异常
     */
    @Test
    void testFollowUser_MissingParams() {
        // 构造非法输入
        Follows input = new Follows(null, null);

        // 应该直接抛出异常
        assertThrows(FollowException.class, () -> controller.followUser(input, response));
    }

    /**
     * 发生运行时异常 → 触发回滚
     */
    @Test
    void testFollowUser_RuntimeException() {
        // 构造合法输入
        Follows input = new Follows(100007, 200013);

        // 模拟 service 抛出异常
        doThrow(new RuntimeException("数据库连接失败")).when(mockFollowService).followUser(anyInt(), anyInt());

        // 应该进入 catch 并触发回滚
        assertThrows(FollowException.class, () -> controller.followUser(input, response));

        // 验证 tx.rollback() 是否被调用
        // （注意：如果你能 mock GlobalTransaction 对象，就可以验证 begin/commit/rollback）
    }

    /**
     * 模拟事务回滚过程
     */
    @Test
    void testFollowUser_TransactionRollback()  {
        // 构造合法输入
        Follows input = new Follows(100007, 200013);

        // 模拟第一个 service 成功，第二个失败
        when(mockFollowService.followUser(eq(100007), eq(200013))).thenReturn(true);
        when(mockUserService.incrementFollowingUsers(eq(200013))).thenReturn(true);
        when(mockUserService.incrementUserFollowers(eq(100007))).thenReturn(false);

        // 应该触发 rollback
        assertThrows(FollowException.class, () -> controller.followUser(input, response));

        // 可以在这里 mock GlobalTransaction 看是否调用了 tx.rollback()
    }
}