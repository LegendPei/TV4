import com.peitianbao.www.controller.VoucherController;
import com.peitianbao.www.model.Coupon;
import com.peitianbao.www.model.CouponOrder;
import com.peitianbao.www.service.CouponService;
import com.peitianbao.www.util.token.RedisUtil;
import lombok.Getter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import redis.clients.jedis.Jedis;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.concurrent.CountDownLatch;

class VoucherControllerConcurrencyTest {

    private CouponService mockCouponService;
    @Getter
    private RedisUtil mockRedisUtil;
    private VoucherController controller;
    private StringWriter stringWriter;

    @BeforeEach
    void setUp() {
        mockCouponService = mock(CouponService.class);
        mockRedisUtil = mock(RedisUtil.class);
        controller = new VoucherController();
        stringWriter = new StringWriter();
    }

    /**
     * 模拟并发下单，验证是否只有一个人成功
     */
    @Test
    void testConcurrentSecKillWithOneStock() throws Exception {
        // 假设库存为 1
        int couponId = 300000;
        int userId1 = 100007;
        int userId2 = 100008;

        // 构造优惠券信息
        Coupon mockCoupon = new Coupon();
        mockCoupon.setMaxPerUser(1); // 每人最多买 1 张
        when(mockCouponService.getCouponInfo(couponId)).thenReturn(mockCoupon);

        // 模拟 Redis 的行为
        Jedis mockJedis = mock(Jedis.class);
        when(RedisUtil.getJedis()).thenReturn(mockJedis);

        // 第一次调用返回 1（成功）
        when(mockJedis.eval(anyString(), anyList(), anyList())).thenReturn(1L);

        // 第二次调用返回 -2（库存不足）
        when(mockJedis.eval(anyString(), anyList(), anyList()))
                .thenAnswer(invocation -> {
                    // 第一次成功，第二次失败
                    return -2L;
                });

        // 模拟两个线程并发请求
        CountDownLatch latch = new CountDownLatch(2);
        String[] result1 = {""};
        String[] result2 = {""};

        Thread t1 = new Thread(() -> {
            try {
                HttpServletResponse response1 = mock(HttpServletResponse.class);
                PrintWriter writer1 = new PrintWriter(stringWriter);
                when(response1.getWriter()).thenReturn(writer1);

                CouponOrder request1 = new CouponOrder(1,couponId, userId1);
                controller.secKill(request1, response1);

                result1[0] = stringWriter.toString().trim();
            } catch (Exception e) {
                result1[0] = e.getMessage();
            } finally {
                latch.countDown();
            }
        });

        Thread t2 = new Thread(() -> {
            try {
                HttpServletResponse response2 = mock(HttpServletResponse.class);
                PrintWriter writer2 = new PrintWriter(stringWriter);
                when(response2.getWriter()).thenReturn(writer2);

                CouponOrder request2 = new CouponOrder(2,couponId, userId2);
                controller.secKill(request2, response2);

                result2[0] = stringWriter.toString().trim();
            } catch (Exception e) {
                result2[0] = e.getMessage();
            } finally {
                latch.countDown();
            }
        });

        // 启动线程并等待结束
        t1.start();
        t2.start();
        latch.await(); // 等待所有线程完成

        // 验证结果
        assertTrue(result1[0].contains("抢购成功") || result2[0].contains("库存不足"));
        assertTrue(result2[0].contains("抢购成功") || result1[0].contains("库存不足"));

        System.out.println("线程 1 结果：" + result1[0]);
        System.out.println("线程 2 结果：" + result2[0]);
    }

}