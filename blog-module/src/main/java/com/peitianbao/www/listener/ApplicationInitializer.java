package com.peitianbao.www.listener;

import com.peitianbao.www.springframework.annontion.DubboReference;
import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.ioc.BeanFactory;
import com.peitianbao.www.springframework.util.ClassScanner;
import com.peitianbao.www.util.ConnectionPool;
import com.peitianbao.www.util.LoadProperties;
import com.peitianbao.www.util.LoggingFramework;
import com.peitianbao.www.util.SqlSession;
import org.apache.dubbo.config.*;
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
        System.out.println("blog try to begin");

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

            //加载Dubbo和Nacos配置
            String applicationName = context.getInitParameter("dubbo.provider-application.name");
            String registryAddress = context.getInitParameter("dubbo.provider-registry.address");
            String protocolName = context.getInitParameter("dubbo.provider-protocol.name");
            String protocolPort = context.getInitParameter("dubbo.provider-protocol.port");

            if (applicationName == null || registryAddress == null || protocolName == null || protocolPort == null) {
                throw new RuntimeException("缺少必要的 Dubbo 配置，请检查 web.xml 文件");
            }

            initializeDubboProvider(applicationName, registryAddress, protocolName, Integer.parseInt(protocolPort));
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

        if (applicationName == null || registryAddress == null) {
            throw new RuntimeException("Dubbo 配置缺失: applicationName 或 registryAddress 为空");
        }

        // 初始化 ApplicationConfig
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);

        // 初始化 RegistryConfig
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);

        // 初始化 DubboBootstrap 并注册消费者
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(applicationConfig)
                .registry(registryConfig)
                .start();

        // 从 web.xml 中读取超时时间配置
        String timeoutStr = context.getInitParameter("dubbo.consumer.timeout");
        if (timeoutStr != null && !timeoutStr.isEmpty()) {
            int timeout = Integer.parseInt(timeoutStr);
            System.setProperty("dubbo.consumer.timeout", String.valueOf(timeout));
            System.out.println("Global timeout set to: " + timeout + " ms");
        }

        System.out.println("DubboBootstrap success: " + registryAddress);
    }

    /**
     * 处理 @DubboReference 注解的注入
     */
    private void processDubboReferenceAnnotations(ServletContext context) throws Exception {
        List<Class<?>> classes = ClassScanner.getClasses("com.peitianbao.www.controller");

        for (Class<?> clazz : classes) {
            Object instance = BeanFactory.getBean(clazz.getSimpleName());

            if (instance == null) {
                System.out.println("Bean not found for class:" + clazz.getName());
                continue;
            }

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
        String version = dubboReference.version();
        String serviceName = dubboReference.serviceName();

        System.out.println("Injecting @DubboReference for field: " + field.getName() + ", service: " + serviceName);

        // 使用 DubboBootstrap 创建远程服务代理
        ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(fieldType);
        referenceConfig.setVersion(version);

        // 设置注册中心地址
        String registryAddress = context.getInitParameter("dubbo.registry.address");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        referenceConfig.setRegistry(registryConfig);

        // 获取远程服务代理对象
        Object proxyInstance = referenceConfig.get();

        // 注入代理对象
        field.set(bean, proxyInstance);
    }

    private static void initializeDubboProvider(String applicationName, String registryAddress, String protocolName, int protocolPort) {
        System.out.println("begin to Dubbo service");

        try {
            // 创建 Dubbo 配置对象
            ApplicationConfig applicationConfig = new ApplicationConfig();
            applicationConfig.setName(applicationName);

            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(registryAddress);

            ProtocolConfig protocolConfig = new ProtocolConfig();
            protocolConfig.setName(protocolName);
            protocolConfig.setPort(protocolPort);

            // 初始化 DubboBootstrap
            DubboBootstrap bootstrap = DubboBootstrap.getInstance();
            bootstrap.application(applicationConfig)
                    .registry(registryConfig)
                    .protocol(protocolConfig);

            // 扫描指定包路径下的类
            List<Class<?>> classes = ClassScanner.getClasses("com.peitianbao.www.service");

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(DubboService.class)) {
                    System.out.println("find DubboService: " + clazz.getName());

                    // 获取接口类型
                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces.length == 0) {
                        throw new RuntimeException("找不到接口: " + clazz.getName());
                    }

                    Class<?> interfaceClass = interfaces[0];

                    // 从 BeanFactory 获取 Service 实例
                    Object serviceInstance = BeanFactory.getBean(clazz.getSimpleName());
                    if (serviceInstance == null) {
                        throw new RuntimeException("Service instance not found in BeanFactory: " + clazz.getName());
                    }

                    // 创建 ServiceConfig 并注册服务
                    ServiceConfig<Object> service = new ServiceConfig<>();
                    service.setInterface(interfaceClass);
                    service.setRef(serviceInstance);
                    service.setVersion("1.0.0");

                    // 将服务添加到 DubboBootstrap
                    bootstrap.service(service);
                    System.out.println("成功注册 Dubbo 服务: " + interfaceClass.getName());
                }
            }

            // 启动 DubboBootstrap
            bootstrap.start();
            System.out.println("DubboBootstrap 成功启动并连接到 Nacos");
        } catch (Exception e) {
            throw new RuntimeException("Dubbo 注册失败", e);
        }
    }
}