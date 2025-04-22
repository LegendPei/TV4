package com.peitianbao.www.springframework.ioc;

import com.peitianbao.www.springframework.annontion.*;
import com.peitianbao.www.springframework.util.ClassScanner;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author leg
 */
public class BeanFactory {

    private static final Map<String, Object> MAP = new ConcurrentHashMap<>();
    private static final Map<Class<?>, Object> INTERFACE_MAP = new ConcurrentHashMap<>();

    public static Map<String, Object> getMap() {
        return MAP;
    }

    public static void initialize(String basePackage) {
        try {
            //扫描包路径下的所有类
            List<Class<?>> classes = ClassScanner.getClasses(basePackage);

            for (Class<?> clazz : classes) {
                if (clazz.isAnnotationPresent(Service.class)) {
                    System.out.println("find Service：" + clazz.getName());
                    registerBean(clazz, clazz.getAnnotation(Service.class).value());
                } else if (clazz.isAnnotationPresent(Dao.class)) {
                    System.out.println("find Dao：" + clazz.getName());
                    registerBean(clazz, clazz.getAnnotation(Dao.class).value());
                } else if (clazz.isAnnotationPresent(Controller.class)) {
                    System.out.println("find controller: " + clazz.getName());
                    registerBean(clazz, clazz.getSimpleName());
                }
            }

            //处理@Autowired注解
            processAutowiredAnnotations();
        } catch (Exception e) {
            throw new RuntimeException("BeanFactory failed", e);
        }
    }

    public static void registerBean(Class<?> clazz, String beanName) throws Exception {
        if (MAP.containsKey(beanName)) {
            //如果实例已经存在，直接返回
            return;
        }

        Constructor<?>[] constructors = clazz.getDeclaredConstructors();
        Constructor<?> injectableConstructor = null;

        //查找带有@Autowired注解的构造函数
        for (Constructor<?> constructor : constructors) {
            if (constructor.isAnnotationPresent(Autowired.class)) {
                injectableConstructor = constructor;
                break;
            }
        }

        Object instance;
        if (injectableConstructor != null) {
            //构造函数注入
            Class<?>[] parameterTypes = injectableConstructor.getParameterTypes();
            Object[] args = new Object[parameterTypes.length];
            for (int i = 0; i < parameterTypes.length; i++) {
                args[i] = findBeanByType(parameterTypes[i]);
            }
            instance = injectableConstructor.newInstance(args);
        } else {
            //默认无参构造函数
            instance = clazz.getDeclaredConstructor().newInstance();
        }

        MAP.put(beanName, instance);

        //如果实现了接口，绑定到INTERFACE_MAP
        for (Class<?> iFace : clazz.getInterfaces()) {
            INTERFACE_MAP.put(iFace, instance);
        }
    }

    public static void registerBean(Class<?> clazz, String beanName, Object instance) {
        MAP.put(beanName, instance);

        //如果实现了接口，绑定到INTERFACE_MAP
        for (Class<?> iFace : clazz.getInterfaces()) {
            INTERFACE_MAP.put(iFace, instance);
        }
    }

    private static void processAutowiredAnnotations() throws Exception {
        for (Object bean : MAP.values()) {
            Class<?> clazz = bean.getClass();

            //注入字段
            for (Field field : clazz.getDeclaredFields()) {
                if (field.isAnnotationPresent(Autowired.class)) {
                    field.setAccessible(true);
                    Class<?> fieldType = field.getType();
                    Object dependency = findBeanByType(fieldType);
                    if (dependency == null) {
                        throw new RuntimeException("can not find " + fieldType.getName() + " Bean");
                    }
                    field.set(bean, dependency);
                }
            }
        }
    }

    private static Object findBeanByType(Class<?> type) {
        if (type.isInterface()) {
            return INTERFACE_MAP.get(type);
        }
        for (Object bean : MAP.values()) {
            if (type.isAssignableFrom(bean.getClass())) {
                return bean;
            }
        }
        return null;
    }

    public static Object getBean(String id) {
        return MAP.get(id);
    }
}