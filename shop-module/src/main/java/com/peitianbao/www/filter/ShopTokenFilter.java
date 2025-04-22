package com.peitianbao.www.filter;

import com.peitianbao.www.util.ResponseUtil;
import com.peitianbao.www.util.token.JwtUtil;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author leg
 */
@WebFilter(filterName = "ShopTokenFilter", urlPatterns = "/ShopService/*")
public class ShopTokenFilter implements Filter {

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain)
            throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取请求路径
        String path = request.getRequestURI();

        //跳过登录接口
        if (path.endsWith("/login")|| path.endsWith("/register")) {
            filterChain.doFilter(request, response);
            return;
        }

        //获取Authorization Header
        String authorizationHeader = request.getHeader("Authorization");
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            ResponseUtil.sendErrorResponse(response, 401, "Missing or invalid Authorization header");
            return;
        }

        //提取Token
        String token = authorizationHeader.substring("Bearer ".length()).trim();
        if (token.isEmpty()) {
            ResponseUtil.sendErrorResponse(response, 401, "Token is empty");
            return;
        }

        //解析Token获取shopId
        Integer shopId = JwtUtil.parseUserIdFromToken(token);
        if (shopId == null) {
            ResponseUtil.sendErrorResponse(response, 401, "Invalid or expired token");
            return;
        }

        //将shopId存入请求上下文
        request.setAttribute("shopId", shopId);

        //放行请求
        filterChain.doFilter(request, response);
    }

}
