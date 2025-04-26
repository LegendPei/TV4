package com.peitianbao.www.listener;

import com.peitianbao.www.springframework.ioc.BeanFactory;
import com.peitianbao.www.util.ConnectionPool;
import com.peitianbao.www.util.LoadProperties;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.Properties;

/**
 * @author leg
 */
public class ApplicationInitializer implements ServletContextListener {

    private static boolean initialized = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("voucher try to begin");

        try {
            if (initialized) {
                System.out.println("voucher ApplicationInitializer have begin");
                return;
            }

            //加载配置文件
            Properties properties = LoadProperties.load("application.properties");
            for (String key : properties.stringPropertyNames()) {
                System.setProperty(key, properties.getProperty(key));
            }

            //初始化连接池
            ConnectionPool connectionPool = new ConnectionPool("application.properties");
            //注册连接池到BeanFactory
            BeanFactory.registerBean(ConnectionPool.class, "connectionPool", connectionPool);
            //创建SqlSession并注册到容器
            SqlSession sqlSession = new SqlSession(connectionPool);
            BeanFactory.registerBean(SqlSession.class, "sqlSession", sqlSession);

            //初始化框架，扫描并注册所有Controller、Service和Dao
            BeanFactory.initialize("com.peitianbao.www");

            //标记为已初始化
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("应用初始化失败", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("turning off user service");
        //关闭连接池和其他资源
        try {
            //获取连接池实例
            ConnectionPool connectionPool = (ConnectionPool) BeanFactory.getBean("connectionPool");
            if (connectionPool != null) {
                connectionPool.close();
            }
        } catch (Exception e) {
            LoggingFramework.severe("关闭连接池时出错: " + e.getMessage());
            LoggingFramework.logException(e);
        }
    }
}