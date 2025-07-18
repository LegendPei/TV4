package com.peitianbao.www.listener;

import com.peitianbao.www.springframework.annontion.DubboReference;
import com.peitianbao.www.springframework.ioc.BeanFactory;
import com.peitianbao.www.springframework.util.ClassScanner;
import com.peitianbao.www.util.*;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.bootstrap.DubboBootstrap;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.sql.DataSource;
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
        System.out.println("Using Gson instance from GsonFactory: " + GsonFactory.getGSON());
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
            String applicationId = "like-service";
            String txServiceGroup = "my_test_tx_group";

            SeataClientBootstrap.init(applicationId, txServiceGroup);

            System.out.println("Seata 客户端已成功初始化");

            //初始化连接池
            ConnectionPool connectionPool = new ConnectionPool("application.properties");

            //包装为标准数据源
            DataSource rawDataSource = new PooledDataSource(connectionPool);

            //是否启用Seata？
            boolean enableSeata = Boolean.parseBoolean(System.getProperty("seata.enabled", "false"));
            DataSource finalDataSource = enableSeata
                    ? SeataClientBootstrap.wrapDataSource(rawDataSource)
                    : rawDataSource;

            //注册到容器中
            BeanFactory.registerBean(DataSource.class, "connectionPool", finalDataSource);

            //创建SqlSession并注册到容器
            SqlSession sqlSession = new SqlSession(finalDataSource);
            BeanFactory.registerBean(SqlSession.class, "sqlSession", sqlSession);

            //初始化框架，扫描并注册所有Controller、Service和Dao
            BeanFactory.initialize("com.peitianbao.www");

            //获取ServletContext
            ServletContext context = sce.getServletContext();

            //初始化Dubbo配置
            initializeDubbo(context);

            //处理@DubboReference注解的注入
            processDubboReferenceAnnotations(context);
            //标记为已初始化
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

        //初始化ApplicationConfig
        ApplicationConfig applicationConfig = new ApplicationConfig();
        applicationConfig.setName(applicationName);

        //初始RegistryConfig
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);

        //初始化DubboBootstrap并注册消费者
        DubboBootstrap bootstrap = DubboBootstrap.getInstance();
        bootstrap.application(applicationConfig)
                .registry(registryConfig)
                .start();

        //从web.xml中读取超时时间配置
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

        //使用DubboBootstrap创建远程服务代理
        ReferenceConfig<?> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(fieldType);
        referenceConfig.setVersion(version);

        //设置注册中心地址
        String registryAddress = context.getInitParameter("dubbo.registry.address");
        RegistryConfig registryConfig = new RegistryConfig();
        registryConfig.setAddress(registryAddress);
        referenceConfig.setRegistry(registryConfig);

        //获取远程服务代理对象
        Object proxyInstance = referenceConfig.get();

        //注入代理对象
        field.set(bean, proxyInstance);
    }
}