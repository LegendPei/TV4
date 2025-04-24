package com.peitianbao.www.listener;

import com.peitianbao.www.springframework.annontion.DubboReference;
import com.peitianbao.www.springframework.ioc.BeanFactory;
import com.peitianbao.www.springframework.util.ClassScanner;
import com.peitianbao.www.util.ConnectionPool;
import com.peitianbao.www.util.LoadProperties;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Properties;

/**
 * @author leg
 */
public class ApplicationInitializer implements ServletContextListener {

    private static boolean initialized = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("like try to begin");

        try {
            if (initialized) {
                System.out.println("ApplicationInitializer have begin");
                return;
            }

            // 加载配置文件
            Properties properties = LoadProperties.load("application.properties");
            for (String key : properties.stringPropertyNames()) {
                System.setProperty(key, properties.getProperty(key));
            }

            // 初始化连接池
            ConnectionPool connectionPool = new ConnectionPool("application.properties");
            BeanFactory.registerBean(ConnectionPool.class, "connectionPool", connectionPool);

            // 创建 SqlSession 并注册到容器
            SqlSession sqlSession = new SqlSession(connectionPool);
            BeanFactory.registerBean(SqlSession.class, "sqlSession", sqlSession);

            // 初始化框架，扫描并注册所有 Controller、Service 和 Dao
            BeanFactory.initialize("com.peitianbao.www");

            // 获取 ServletContext
            ServletContext context = sce.getServletContext();

            // 初始化 Dubbo 配置
            initializeDubbo(context);

            // 处理 @DubboReference 注解的注入
            processDubboReferenceAnnotations(context);

            // 标记为已初始化
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("应用初始化失败", e);
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("turning off like service");
        try {
            ConnectionPool connectionPool = (ConnectionPool) BeanFactory.getBean("connectionPool");
            if (connectionPool != null) {
                connectionPool.close();
            }
        } catch (Exception e) {
            LoggingFramework.severe("关闭连接池时出错: " + e.getMessage());
            LoggingFramework.logException(e);
        }
    }

    /**
     * 初始化 Dubbo 配置
     */
    private void initializeDubbo(ServletContext context) {
        String applicationName = context.getInitParameter("dubbo.application.name");
        String registryAddress = context.getInitParameter("dubbo.registry.address");

        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);

        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(applicationConfig)
                .registry(registryConfig)
                .start();

        System.out.println("DubboBootstrap success to Nacos");
    }

    /**
     * 处理 @DubboReference 注解的注入
     */
    private void processDubboReferenceAnnotations(ServletContext context) throws Exception {
        List<Class<?>> classes = ClassScanner.getClasses("com.peitianbao.www.controller");

        for (Class<?> clazz : classes) {
            Object instance = BeanFactory.getBean(clazz.getSimpleName());

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(DubboReference.class)) {
                    handleDubboReferenceField(field, instance, context);
                }
            }
        }
    }

    /**
     * 处理单个 @DubboReference 注解字段
     */
    private void handleDubboReferenceField(Field field, Object bean, ServletContext context) throws Exception {
        field.setAccessible(true);
        Class<?> fieldType = field.getType();

        DubboReference dubboReference = field.getAnnotation(DubboReference.class);
        String serviceName = dubboReference.serviceName();
        String version = dubboReference.version();

        // 动态加载模块配置
        String applicationName = getApplicationNameFromConfig(serviceName, context);
        if (applicationName == null) {
            throw new RuntimeException("未找到服务配置: " + serviceName);
        }

        // 使用 DubboBootstrap 创建远程服务代理
        ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(fieldType);
        referenceConfig.setVersion(version);

        // 设置全局 DubboBootstrap 配置
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(new ApplicationConfig(applicationName))
                .registry(new RegistryConfig(getRegistryAddress(context)));

        // 获取远程服务代理对象
        Object proxyInstance = referenceConfig.get();

        // 注入代理对象
        field.set(bean, proxyInstance);
    }

    /**
     * 根据服务名称获取应用名称
     */
    private String getApplicationNameFromConfig(String serviceName, ServletContext context) {
        return context.getInitParameter(serviceName + ".dubbo.application.name");
    }

    /**
     * 获取注册中心地址
     */
    private String getRegistryAddress(ServletContext context) {
        return context.getInitParameter("dubbo.registry.address");
    }
}