import com.peitianbao.www.dao.UserDao;
import com.peitianbao.www.exception.UserException;
import com.peitianbao.www.model.po.UsersPO;
import com.peitianbao.www.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {
    // Mock 对象：模拟 UserDao 的行为
    @Mock
    private UserDao userDao;

    // 注入 Mock 对象到被测类中
    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        // 初始化 Mock 对象
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testUserRegister_Success() {
        // 准备测试数据
        String userName = "testUser";
        String userPassword = "password123";
        String userAccount = "testAccount";

        // 模拟 userDao.insertUser 返回 true
        when(userDao.insertUser(any(UsersPO.class))).thenReturn(true);

        // 执行方法
        boolean result = userService.userRegister(userName, userPassword, userAccount);

        // 验证结果
        assertTrue(result);

        // 验证 userDao.insertUser 是否被调用了一次
        verify(userDao, times(1)).insertUser(any(UsersPO.class));
    }

    @Test
    void testUserRegister_Failure() {
        // 准备测试数据
        String userName = "testUser";
        String userPassword = "password123";
        String userAccount = "testAccount";

        // 模拟 userDao.insertUser 返回 false
        when(userDao.insertUser(any(UsersPO.class))).thenReturn(false);

        // 执行方法并验证是否抛出异常
        UserException exception = assertThrows(UserException.class, () -> {
            userService.userRegister(userName, userPassword, userAccount);
        });

        // 验证异常消息
        assertEquals("用户注册失败:已注册的账号或名字", exception.getMessage());

        // 验证 userDao.insertUser 是否被调用了一次
        verify(userDao, times(1)).insertUser(any(UsersPO.class));
    }
}
