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

            String applicationId = "blog-service";
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

            // 使用统一的 ApplicationConfig（避免冲突）
            String applicationName = context.getInitParameter("dubbo.application.name");
            String registryAddress = context.getInitParameter("dubbo.registry.address");
            String protocolName = context.getInitParameter("dubbo.protocol.name");
            String protocolPortStr = context.getInitParameter("dubbo.protocol.port");

            if (applicationName == null || registryAddress == null || protocolName == null || protocolPortStr == null) {
                throw new RuntimeException("缺少必要的 Dubbo 配置，请检查 web.xml 文件");
            }

            int protocolPort = Integer.parseInt(protocolPortStr);

            // 初始化 ApplicationConfig
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

            // 注册本地服务
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

                    // 创建 ServiceConfig
                    ServiceConfig<Object> service = new ServiceConfig<>();
                    service.setInterface(interfaces[0]);
                    service.setRef(serviceImpl);
                    service.setVersion("1.0.0");

                    // 注册服务到 DubboBootstrap
                    bootstrap.service(service);
                    System.out.println("成功注册 Dubbo 服务: " + interfaces[0].getName());
                }
            }

            // 初始化 Dubbo Consumer
            initializeDubboReference(context, bootstrap);

            // 启动 DubboBootstrap
            bootstrap.start();
            System.out.println("DubboBootstrap 成功启动并连接到 Nacos");

            // 处理 Controller 中的 @DubboReference 注解
            processDubboReferenceAnnotations(context);

            // 标记为已初始化
            initialized = true;
        } catch (Exception e) {
            throw new RuntimeException("应用初始化失败", e);
        }
    }

    /**
     * 初始化 DubboConsumer，引用远程服务
     */
    private void initializeDubboReference(ServletContext context, DubboBootstrap bootstrap) throws Exception {
        // 手动定义要引用的服务列表
        List<String> referenceServices = List.of(
                "com.peitianbao.www.api.FollowService"
        );

        for (String serviceName : referenceServices) {
            ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();
            referenceConfig.setInterface(Class.forName(serviceName));
            referenceConfig.setVersion("1.0.0");

            // 设置注册中心地址
            String registryAddress = context.getInitParameter("dubbo.registry.address");
            if (registryAddress != null && !registryAddress.isEmpty()) {
                RegistryConfig registryConfig = new RegistryConfig();
                registryConfig.setAddress(registryAddress);
                referenceConfig.setRegistry(registryConfig);
            }

            // 将引用服务加入 DubboBootstrap
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
                    DubboReference dubboReference = field.getAnnotation(DubboReference.class);
                    handleDubboReferenceField(field, instance, context, dubboReference.version(), dubboReference.serviceName());
                }
            }
        }
    }

    /**
     * 处理单个 @DubboReference 字段
     */
    private void handleDubboReferenceField(Field field, Object bean, ServletContext context, String version, String serviceName) throws Exception {
        field.setAccessible(true);
        Class<?> fieldType = field.getType();

        // 直接通过 DubboBootstrap 引用服务
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
        field.set(bean, proxyInstance);
    }
}