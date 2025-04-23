package com.peitianbao.www.listener;

import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.ioc.BeanFactory;
import com.peitianbao.www.springframework.util.ClassScanner;
import com.peitianbao.www.util.ConnectionPool;
import com.peitianbao.www.util.LoadProperties;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ProtocolConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;
import java.util.Properties;

/**
 * @author leg
 */
public class ApplicationInitializer implements ServletContextListener {

    private static boolean initialized = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("try to begin");

        try {
            if (initialized) {
                System.out.println("ApplicationInitializer have begin");
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

            //获取ServletContext
            ServletContext context = sce.getServletContext();

            //加载Dubbo和Nacos配置
            String applicationName = context.getInitParameter("dubbo.application.name");
            String registryAddress = context.getInitParameter("dubbo.registry.address");
            String protocolName = context.getInitParameter("dubbo.protocol.name");
            String protocolPort = context.getInitParameter("dubbo.protocol.port");

            if (applicationName == null || registryAddress == null || protocolName == null || protocolPort == null) {
                throw new RuntimeException("缺少必要的 Dubbo 配置，请检查 web.xml 文件");
            }

            //初始化Dubbo配置
            initializeDubbo(applicationName, registryAddress, protocolName, Integer.parseInt(protocolPort));

            //标记为已初始化
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("应用初始化失败", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("turning off shop service");
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

    private static void initializeDubbo(String applicationName, String registryAddress, String protocolName, int protocolPort) {
        System.out.println("begin to Dubbo service");

        try {
            //创建Dubbo配置对象
            ApplicationConfig applicationConfig = new ApplicationConfig();
            applicationConfig.setName(applicationName);

            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(registryAddress);

            ProtocolConfig protocolConfig = new ProtocolConfig();
            protocolConfig.setName(protocolName);
            protocolConfig.setPort(protocolPort);

            //初始化DubboBootstrap
            DubboBootstrap bootstrap = DubboBootstrap.getInstance();
            bootstrap.application(applicationConfig)
                    .registry(registryConfig)
                    .protocol(protocolConfig);

            //扫描指定包路径下的类
            List<Class<?>> classes = ClassScanner.getClasses("com.peitianbao.www.service");

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(DubboService.class)) {
                    System.out.println("find DubboService:" + clazz.getName());

                    //获取接口类型
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces.length == 0) {
                        throw new RuntimeException("no find interface" + clazz.getName());
                    }

                    Class<?> interfaceClass = interfaces[0];

                    //创建ServiceConfig并注册服务
                    ServiceConfig<Object> service = new ServiceConfig<>();
                    service.setInterface(interfaceClass);
                    service.setRef(clazz.getDeclaredConstructor().newInstance());
                    service.setVersion("1.0.0");

                    //将服务添加到DubboBootstrap
                    bootstrap.service(service);
                    System.out.println("success Dubbo service:" + interfaceClass.getName());
                }
            }

            //启动DubboBootstrap
            bootstrap.start();
            System.out.println("DubboBootstrap success to Nacos");
        } catch (Exception e) {
            throw new RuntimeException("Dubbo register failed", e);
        }
    }
}