package com.peitianbao.www.util;

import com.google.gson.Gson;
import com.peitianbao.www.model.ApiResponse;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author leg
 */
public class ResponseUtil {
    public static void sendSuccessResponse(HttpServletResponse resp, Object data) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Gson gson = GsonFactory.getGSON();

        ApiResponse response = new ApiResponse(200, "Success", data);
        resp.getWriter().write(gson.toJson(response));
    }

    public static void sendErrorResponse(HttpServletResponse resp, int statusCode, String message) throws IOException {
        resp.setStatus(statusCode);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        Gson gson = GsonFactory.getGSON();

        ApiResponse response = new ApiResponse(statusCode, message, null);
        resp.getWriter().write(gson.toJson(response));
    }
}
