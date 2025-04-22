package com.peitianbao.www.springframework.ioc;

import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.util.ClassScanner;
import com.peitianbao.www.util.ConnectionPool;
import com.peitianbao.www.util.LoadProperties;
import com.peitianbao.www.util.SqlSession;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;


import java.io.InputStream;
import java.util.List;
import java.util.Properties;

/**
 * @author leg
 */
public class ApplicationInitializer {

    private static boolean initialized = false;
    private static final DubboBootstrap BOOTSTRAP = DubboBootstrap.getInstance();

    public static void initialize() {
        try {

            //加载配置文件
            Properties properties = LoadProperties.load("application.properties");
            for (String key : properties.stringPropertyNames()) {
                System.setProperty(key, properties.getProperty(key));
            }

            //初始化连接池
            ConnectionPool connectionPool = new ConnectionPool("application.properties");

            //创建SqlSession并注册到容器
            SqlSession sqlSession = new SqlSession(connectionPool);
            BeanFactory.registerBean(SqlSession.class, "sqlSession", sqlSession);

            //初始化框架，扫描并注册所有Controller、Service和Dao
            BeanFactory.initialize("com.peitianbao.www");

            //标记为已初始化
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("Application failed", e);
        }
    }
}