package com.peitianbao.www.controller;

import com.google.gson.Gson;
import com.peitianbao.www.springframework.annontion.MyRequestBody;
import com.peitianbao.www.springframework.annontion.RequestMapping;
import com.peitianbao.www.springframework.ioc.BeanFactory;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author leg
 */
@WebServlet("/*")
public class DispatcherServlet extends HttpServlet {
    private final Map<String, Method> handlerMappings = new HashMap<>();

    @Override
    public void init() {
        //初始化框架，扫描并注册所有Controller
        BeanFactory.initialize("com.peitianbao.www");

        //解析@RequestMapping注解，生成 URL 映射
        for (Object bean : BeanFactory.getMap().values()) {
            Class<?> clazz = bean.getClass();
            if (clazz.isAnnotationPresent(com.peitianbao.www.springframework.annontion.Controller.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        String url = requestMapping.value();
                        handlerMappings.put(url, method);
                    }
                }
            }
        }
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        // 获取请求路径
        String path = req.getRequestURI();
        if (path.startsWith(req.getContextPath())) {
            path = path.substring(req.getContextPath().length());
        }

        // 获取请求方法
        String httpMethod = req.getMethod();

        // 查找对应的方法
        Method method = handlerMappings.get(path);
        if (method == null) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            resp.getWriter().write("404 Not Found");
            return;
        }

        // 验证请求方法是否匹配
        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
        if (!requestMapping.methodType().name().equalsIgnoreCase(httpMethod)) {
            resp.setStatus(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            resp.getWriter().write("405 Method Not Allowed");
            return;
        }

        // 获取对应的 Controller
        String controllerName = method.getDeclaringClass().getSimpleName();
        controllerName = controllerName.substring(0, 1).toLowerCase() + controllerName.substring(1);
        Object controller = BeanFactory.getBean(controllerName);

        if (controller == null) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error: Controller not found");
            return;
        }

        try {
            // 处理方法参数
            Object[] args = resolveMethodArguments(method, req);

            // 调用方法并返回结果
            Object result = method.invoke(controller, args);
            resp.getWriter().write(result.toString());
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            resp.getWriter().write("Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * 解析方法参数
     */
    private Object[] resolveMethodArguments(Method method, HttpServletRequest req) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = new Object[parameterTypes.length];

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] annotations = parameterAnnotations[i];

            // 检查是否有 @MyRequestBody 注解
            boolean hasRequestBody = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof MyRequestBody) {
                    hasRequestBody = true;
                    break;
                }
            }

            if (hasRequestBody) {
                // 使用 Gson 解析请求体中的 JSON 数据
                //JSON数据中的字段名要和实体类的字段名一一对应
                Gson gson = new Gson();
                String json = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                args[i] = gson.fromJson(json, paramType);
            } else {
                throw new RuntimeException("Unsupported parameter type or missing @MyRequestBody");
            }
        }

        return args;
    }
}
