package com.peitianbao.www.filter;


import com.google.gson.Gson;
import com.peitianbao.www.exception.Result;
import com.peitianbao.www.exception.ServiceException;
import com.peitianbao.www.util.GsonFactory;
import com.peitianbao.www.util.LoggingFramework;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author leg
 */

@WebFilter("/*")
public class GlobalExceptionFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException {
        try {
            //继续执行后续的过滤器或Servlet
            chain.doFilter(request, response);
        } catch (ServiceException e) {
            //捕获业务异常
            handleException(response, HttpServletResponse.SC_BAD_REQUEST, 400, e.getMessage());
        } catch (Exception e) {
            //捕获未知异常
            LoggingFramework.severe("GlobalExceptionFilter 捕获了一个未知异常: " + e.getMessage());
            e.printStackTrace();
            LoggingFramework.logException(e);
            handleException(response, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 500, "网络出错");
        }
    }

    private void handleException(ServletResponse response, int httpStatus, int errorCode, String errorMessage)
            throws IOException {
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        httpResponse.setStatus(httpStatus);
        httpResponse.setContentType("application/json;charset=UTF-8");

        //构造统一的错误响应
        Result<?> result = Result.error(errorCode, errorMessage);

        //使用全局单例 Gson 实例
        Gson gson = GsonFactory.getGSON();
        String jsonResponse = gson.toJson(result);

        //写入响应
        PrintWriter writer = response.getWriter();
        writer.write(jsonResponse);
        writer.flush();
    }
}