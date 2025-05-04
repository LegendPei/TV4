package com.peitianbao.www.listener;

import com.peitianbao.www.springframework.annontion.DubboReference;
import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.springframework.ioc.BeanFactory;
import com.peitianbao.www.springframework.util.ClassScanner;
import com.peitianbao.www.util.*;
import org.apache.dubbo.config.*;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * @author leg
 */
public class ApplicationInitializer implements ServletContextListener {

    private static boolean initialized = false;

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("follow try to begin");

        try {
            if (initialized) {
                System.out.println("ApplicationInitializer 已初始化");
                return;
            }

            // 加载配置文件
            Properties properties = LoadProperties.load("application.properties");
            for (String key : properties.stringPropertyNames()) {
                System.setProperty(key, properties.getProperty(key));
            }

            String applicationId = "follow-service";
            String txServiceGroup = "my_test_tx_group";

            SeataClientBootstrap.init(applicationId, txServiceGroup);

            System.out.println("Seata 客户端已成功初始化");
            //初始化连接池
            ConnectionPool connectionPool = new ConnectionPool("application.properties");
            // 包装为标准数据源
            DataSource rawDataSource = new PooledDataSource(connectionPool);

            // 是否启用 Seata？
            boolean enableSeata = Boolean.parseBoolean(System.getProperty("seata.enabled", "false"));
            DataSource finalDataSource = enableSeata
                    ? SeataClientBootstrap.wrapDataSource(rawDataSource)
                    : rawDataSource;
            //注册连接池到BeanFactory
            BeanFactory.registerBean(ConnectionPool.class, "connectionPool", finalDataSource);
            //创建SqlSession并注册到容器
            SqlSession sqlSession = new SqlSession(finalDataSource);
            BeanFactory.registerBean(SqlSession.class, "sqlSession", sqlSession);

            // 初始化框架，扫描并注册所有 Controller、Service 和 Dao
            BeanFactory.initialize("com.peitianbao.www");

            // 获取 ServletContext
            ServletContext context = sce.getServletContext();

            // 使用统一的 ApplicationConfig
            String applicationName = context.getInitParameter("dubbo.application.name");
            String registryAddress = context.getInitParameter("dubbo.registry.address");
            String protocolName = context.getInitParameter("dubbo.protocol.name");
            String protocolPortStr = context.getInitParameter("dubbo.protocol.port");

            if (applicationName == null || registryAddress == null || protocolName == null || protocolPortStr == null) {
                throw new RuntimeException("缺少必要的 Dubbo 配置，请检查 web.xml 文件");
            }

            int protocolPort = Integer.parseInt(protocolPortStr);

            // 创建统一的 ApplicationConfig
            ApplicationConfig applicationConfig = new ApplicationConfig();
            applicationConfig.setName(applicationName);

            RegistryConfig registryConfig = new RegistryConfig();
            registryConfig.setAddress(registryAddress);

            ProtocolConfig protocolConfig = new ProtocolConfig();
            protocolConfig.setName(protocolName);
            protocolConfig.setPort(protocolPort);

            // 初始化 DubboBootstrap（只调用一次 start()）
            DubboBootstrap bootstrap = DubboBootstrap.getInstance();
            bootstrap.application(applicationConfig)
                    .registry(registryConfig)
                    .protocol(protocolConfig);

            // 注册本地服务（Provider）
            List<Class<?>> serviceClasses = ClassScanner.getClasses("com.peitianbao.www.service");
            for (Class<?> clazz : serviceClasses) {
                if (clazz.isAnnotationPresent(DubboService.class)) {
                    Object serviceImpl = BeanFactory.getBean(clazz.getSimpleName());
                    if (serviceImpl == null) {
                        throw new RuntimeException("找不到 Service 实现: " + clazz.getName());
                    }

                    Class<?>[] interfaces = clazz.getInterfaces();
                    if (interfaces.length == 0) {
                        throw new RuntimeException("Service 类没有实现接口: " + clazz.getName());
                    }

                    ServiceConfig<Object> service = new ServiceConfig<>();
                    service.setInterface(interfaces[0]);
                    service.setRef(serviceImpl);
                    service.setVersion("1.0.0");

                    // 注册服务到 DubboBootstrap
                    bootstrap.service(service);
                    System.out.println("成功注册服务: " + interfaces[0].getName());
                }
            }

            // 引用远程服务（Consumer）
            initializeDubboReference(context, bootstrap);

            // 处理 Controller 层中的 @DubboReference 注解
            processDubboReferenceAnnotations(context);

            // 启动 DubboBootstrap（只启动一次）
            bootstrap.start();
            System.out.println("DubboBootstrap 成功启动并连接到 Nacos");

            // 标记为已初始化
            initialized = true;

        } catch (Exception e) {
            throw new RuntimeException("应用初始化失败", e);
        }
    }

    /**
     * 初始化远程服务引用（Consumer）
     */
    private void initializeDubboReference(ServletContext context, DubboBootstrap bootstrap) throws Exception {
        List<String> referenceServices = Arrays.asList(
                "com.peitianbao.www.api.ShopService",
                "com.peitianbao.www.api.UserService"
        );

        for (String serviceName : referenceServices) {
            ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();
            referenceConfig.setInterface(Class.forName(serviceName));
            referenceConfig.setVersion("1.0.0");
            referenceConfig.setCheck(false);
            referenceConfig.setLazy(true);

            // 设置注册中心地址
            String registryAddress = context.getInitParameter("dubbo.registry.address");
            if (registryAddress != null && !registryAddress.isEmpty()) {
                RegistryConfig registryConfig = new RegistryConfig();
                registryConfig.setAddress(registryAddress);
                referenceConfig.setRegistry(registryConfig);
            }

            // 添加引用到 DubboBootstrap
            bootstrap.reference(referenceConfig);
            System.out.println("成功引用远程服务: " + serviceName);
        }
    }

    /**
     * 处理 Controller 层中的 @DubboReference 注解
     */
    private void processDubboReferenceAnnotations(ServletContext context) throws Exception {
        List<Class<?>> controllerClasses = ClassScanner.getClasses("com.peitianbao.www.controller");

        for (Class<?> clazz : controllerClasses) {
            Object instance = BeanFactory.getBean(clazz.getSimpleName());
            if (instance == null) {
                continue;
            }

            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(DubboReference.class)) {
                    handleDubboReference(field, instance, context);
                }
            }
        }
    }

    /**
     * 处理单个 DubboReference 字段
     */
    private void handleDubboReference(Field field, Object bean, ServletContext context) throws Exception {
        field.setAccessible(true);
        Class<?> fieldType = field.getType();

        DubboReference dubboReference = field.getAnnotation(DubboReference.class);
        String version = dubboReference.version();
        String serviceName = dubboReference.serviceName();

        System.out.println("注入 @DubboReference 字段: " + field.getName() + ", 接口: " + fieldType.getName());

        ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(fieldType);
        referenceConfig.setVersion(version);
        referenceConfig.setCheck(false);
        referenceConfig.setLazy(true);

        String registryAddress = context.getInitParameter("dubbo.registry.address");
        if (registryAddress == null || registryAddress.isEmpty()) {
            throw new RuntimeException("未找到 Dubbo 消费者注册中心地址");
        }

        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        referenceConfig.setRegistry(registryConfig);

        // 获取代理对象
        Object proxyInstance = referenceConfig.get();
        field.set(bean, proxyInstance);
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
}