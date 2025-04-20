package com.peitianbao.www.util;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Properties;
import java.util.logging.*;

/**
 * @author leg
 */
public class LoggingFramework {
    private static final Logger logger = Logger.getLogger(LoggingFramework.class.getName());

    //配置日志文件和格式
    static {
        try {
            /*移除默认的处理器，防止在控制台打印两遍
            Logger rootLogger = LogManager.getLogManager().getLogger("");
            Handler[] handlers = rootLogger.getHandlers();
            for (Handler handler : handlers) {
                rootLogger.removeHandler(handler);
            }
            */
            //加载配置文件
            String configFileName = "application.properties";
            Properties properties = LoadProperties.load(configFileName);
            String workingDir = properties.getProperty("logging.path");
            System.out.println("Working Directory = " + workingDir);

            //配置日志文件路径
            String logFilePath = workingDir + "/logs/app-" + new SimpleDateFormat("yyyy-MM-dd").format(new Date()) + ".log";
            System.out.println("Log File Path = " + logFilePath);

            File logDir = new File(logFilePath).getParentFile();
            if (logDir != null && !logDir.exists()) {
                boolean created = logDir.mkdirs();
                if (!created) {
                    throw new IOException("Failed to create log directory: " + logDir.getAbsolutePath());
                }
            }

            //添加文件处理器并设置自定义格式
            FileHandler fileHandler = new FileHandler(logFilePath, true);
            fileHandler.setFormatter(new LogFormatter());
            fileHandler.setLevel(Level.ALL);
            logger.addHandler(fileHandler);

            /*添加控制台处理器并设置自定义格式
            ConsoleHandler consoleHandler = new ConsoleHandler();
            consoleHandler.setFormatter(new LogFormatter());
            consoleHandler.setLevel(Level.INFO);
            logger.addHandler(consoleHandler);
            */
            logger.setLevel(Level.ALL);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error during logging initialization: " + e.getMessage(), e);
        }
    }

    //日志等级
    public static void finest(String message) {
        logger.log(Level.FINEST, message);
    }

    public static void finer(String message) {
        logger.log(Level.FINER, message);
    }

    public static void fine(String message) {
        logger.log(Level.FINE, message);
    }

    public static void config(String message) {
        logger.log(Level.CONFIG, message);
    }

    public static void info(String message) {
        logger.log(Level.INFO, message);
    }

    public static void warning(String message) {
        logger.log(Level.WARNING, message);
    }

    public static void severe(String message) {
        logger.log(Level.SEVERE, message);
    }

    public static void logException(Throwable e) {
        logger.log(Level.SEVERE, "Exception details:", e);
    }

    //动态代理实现AOP
    public static <T> T createProxy(T target, Class<T> interfaceType) {
        return interfaceType.cast(Proxy.newProxyInstance(
                target.getClass().getClassLoader(),
                new Class<?>[]{interfaceType},
                new LoggingInvocationHandler(target)
        ));
    }

    //InvocationHandler实现
    private record LoggingInvocationHandler(Object target) implements InvocationHandler {

        @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                //拿到方法名和类名
                String methodName = method.getName();
                String className = target.getClass().getSimpleName();

                //检查方法是否有@Log注解，有就在日志中记录传入方法的参数和调用的方法
                if (method.isAnnotationPresent(Log.class)) {
                    System.out.println("Method is annotated with @Log");
                    Log logAnnotation = method.getAnnotation(Log.class);

                    //记录方法调用前的日志
                    if (logAnnotation.logArguments()) {
                        info(String.format("Calling %s.%s with arguments: %s", className, methodName, Arrays.toString(args)));
                    }

                    //自定义日志消息
                    if (!logAnnotation.message().isEmpty()) {
                        info(logAnnotation.message());
                    }
                } else {
                    System.out.println("Method is NOT annotated with @Log");
                }

                Object result;
                try {
                    result = method.invoke(target, args);
                    if (method.isAnnotationPresent(Log.class)) {
                        Log logAnnotation = method.getAnnotation(Log.class);
                        if (logAnnotation.logResult()) {
                            info(String.format("%s.%s returned: %s", className, methodName, result));
                        }
                    }
                } catch (Exception e) {
                    //捕获异常并记录详细日志：异常摘要和堆栈信息，并重新抛出异常
                    severe(String.format("Exception in %s.%s: %s", className, methodName, e.getMessage()));
                    logException(e);
                    throw e;
                }
                return result;
            }
        }

    //注解,可以以选择是否记录参数，是否记录返回值，自定义日志消息
    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.METHOD)
    public @interface Log {
        boolean logArguments() default true;
        boolean logResult() default true;
        String message() default "";
    }
}