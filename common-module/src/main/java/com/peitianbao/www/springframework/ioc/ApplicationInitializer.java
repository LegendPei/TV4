package com.peitianbao.www.springframework.ioc;

import com.peitianbao.www.springframework.annontion.DubboService;
import com.peitianbao.www.util.ConnectionPool;
import com.peitianbao.www.util.LoadProperties;
import com.peitianbao.www.util.SqlSession;
import org.apache.dubbo.config.ApplicationConfig;
import org.apache.dubbo.config.RegistryConfig;
import org.apache.dubbo.config.ServiceConfig;


import java.util.Properties;

/**
 * @author leg
 */
public class ApplicationInitializer {

    public static void initialize() {
        try {
            // 加载配置文件
            Properties properties = LoadProperties.load("application.properties");
            for (String key : properties.stringPropertyNames()) {
                System.setProperty(key, properties.getProperty(key));
            }

            // 初始化连接池
            ConnectionPool connectionPool = new ConnectionPool("application.properties");

            // 创建 SqlSession 并注册到容器
            SqlSession sqlSession = new SqlSession(connectionPool);
            BeanFactory.registerBean(SqlSession.class, "sqlSession", sqlSession);

            // 初始化框架，扫描并注册所有 Controller、Service 和 Dao
            BeanFactory.initialize("com.peitianbao.www");

            // 初始化 Dubbo 服务
            initializeDubbo();
        } catch (Exception e) {
            throw new RuntimeException("Application 初始化失败", e);
        }
    }

    private static void initializeDubbo(){
        // 获取 Dubbo 配置
        String applicationName = System.getProperty("dubbo.application.name");
        String registryAddress = System.getProperty("dubbo.registry.address");

        if (applicationName == null || registryAddress == null) {
            throw new RuntimeException("Dubbo 配置缺失，请检查 application.properties");
        }

        // 注册 Dubbo 服务
        for (Object bean : BeanFactory.getMap().values()) {
            Class<?> clazz = bean.getClass();
            if (clazz.isAnnotationPresent(DubboService.class)) {
                registerDubboService(bean, applicationName, registryAddress);
            }
        }
    }

    private static void registerDubboService(Object bean, String applicationName, String registryAddress) {
        try {
            ApplicationConfig application = new ApplicationConfig();
            application.setName(applicationName);

            RegistryConfig registry = new RegistryConfig();
            registry.setAddress(registryAddress);

            ServiceConfig<Object> service = new ServiceConfig<>();
            service.setApplication(application);
            service.setRegistry(registry);
            service.setInterface(getServiceInterface(bean.getClass()));
            service.setRef(bean);
            service.setVersion("1.0.0");

            service.export();
            System.out.println("Dubbo 服务已注册：" + bean.getClass().getName());
        } catch (Exception e) {
            throw new RuntimeException("Dubbo 服务注册失败：" + bean.getClass().getName(), e);
        }
    }

    private static Class<?> getServiceInterface(Class<?> clazz) {
        for (Class<?> iFace : clazz.getInterfaces()) {
            return iFace;
        }
        throw new RuntimeException("未找到接口：" + clazz.getName());
    }
}