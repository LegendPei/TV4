package com.peitianbao.www.listener;

import com.google.gson.Gson;
import com.peitianbao.www.exception.CommentException;
import com.peitianbao.www.springframework.annontion.Controller;
import com.peitianbao.www.springframework.annontion.MyRequestBody;
import com.peitianbao.www.springframework.annontion.RequestMapping;
import com.peitianbao.www.springframework.ioc.BeanFactory;
import com.peitianbao.www.util.GsonFactory;
import com.peitianbao.www.util.ResponseUtil;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
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
        //解析@RequestMapping注解，生成URL映射
        for (Object bean : BeanFactory.getMap().values()) {
            Class<?> clazz = bean.getClass();
            if (clazz.isAnnotationPresent(Controller.class)) {
                for (Method method : clazz.getDeclaredMethods()) {
                    if (method.isAnnotationPresent(RequestMapping.class)) {
                        RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                        String url = requestMapping.value();
                        handlerMappings.put(url, method);
                        System.out.println("find url：" + url + " == " + method.getName());
                    }
                }
            }
        }
        System.out.println("DispatcherServlet initialized with handler mappings: " + handlerMappings.size());
    }

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        //获取请求路径
        String path = req.getRequestURI();
        if (path.startsWith(req.getContextPath())) {
            path = path.substring(req.getContextPath().length());
        }

        //查找对应的方法
        Method method = handlerMappings.get(path);
        if (method == null) {
            ResponseUtil.sendErrorResponse(resp, 404, "Not Found");
            return;
        }

        try {
            //验证请求方法是否匹配
            RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
            if (!requestMapping.methodType().name().equalsIgnoreCase(req.getMethod())) {
                ResponseUtil.sendErrorResponse(resp, 405, "Method Not Allowed");
                return;
            }

            //获取对应的Controller
            String controllerName = method.getDeclaringClass().getSimpleName();
            Object controller = BeanFactory.getBean(controllerName);

            if (controller == null) {
                ResponseUtil.sendErrorResponse(resp, 500, "Internal Server Error: Controller not found");
                return;
            }

            //处理方法参数
            Object[] args = resolveMethodArguments(method, req,resp);

            //调用方法并返回结果
            Object result = method.invoke(controller, args);
        } catch (InvocationTargetException e) {
            //捕获目标方法抛出的异常
            Throwable cause = e.getCause();
            if (cause instanceof CommentException commentException) {
                handleShopException(resp, commentException);
            } else {
                ResponseUtil.sendErrorResponse(resp, 500, "Internal Server Error: " + cause.getMessage());
            }
        } catch (Exception e) {
            ResponseUtil.sendErrorResponse(resp, 500, "Internal Server Error: " + e.getMessage());
        }
    }

    /**
     * 处理 CommentException
     */
    private void handleShopException(HttpServletResponse resp, CommentException commentException) throws IOException {
        String message = commentException.getMessage();
        if (message != null && message.matches("^\\[\\d{3}].*")) {
            int code = Integer.parseInt(message.substring(1, 4));
            String errorMessage = message.substring(6).trim();
            ResponseUtil.sendErrorResponse(resp, code, errorMessage);
        } else {
            ResponseUtil.sendErrorResponse(resp, 500, "Internal Server Error: " + message);
        }
    }

    /**
     * 解析方法参数
     */
    private Object[] resolveMethodArguments(Method method, HttpServletRequest req, HttpServletResponse resp) throws Exception {
        Class<?>[] parameterTypes = method.getParameterTypes();
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Object[] args = new Object[parameterTypes.length];

        //使用全局单例
        Gson gson = GsonFactory.getGSON();

        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> paramType = parameterTypes[i];
            Annotation[] annotations = parameterAnnotations[i];

            boolean hasRequestBody = false;
            for (Annotation annotation : annotations) {
                if (annotation instanceof MyRequestBody) {
                    hasRequestBody = true;
                    break;
                }
            }

            if (hasRequestBody) {
                String json = req.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
                try {
                    args[i] = gson.fromJson(json, paramType);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to parse request body: " + e.getMessage(), e);
                }
            } else if (paramType.isAssignableFrom(HttpServletRequest.class)) {
                args[i] = req;
            } else if (paramType.isAssignableFrom(HttpServletResponse.class)) {
                args[i] = resp;
            } else {
                throw new RuntimeException("Unsupported parameter type or missing @MyRequestBody");
            }
        }

        return args;
    }
}