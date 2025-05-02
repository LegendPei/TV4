package com.peitianbao.www.controller;

import com.peitianbao.www.util.ResponseUtil;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * @author leg
 */
@WebServlet("/uploadBlogMedia")
@MultipartConfig(
        fileSizeThreshold = 1024 * 1024 * 2, //1MB缓存
        maxFileSize = 1024 * 1024 * 10,     //单个文件最大10MB
        maxRequestSize = 1024 * 1024 * 50   //整体请求最大50MB
)
public class UploadServlet extends HttpServlet {
    // 使用绝对路径
    private static final String UPLOAD_DIR = "C:/Users/leg/Desktop/upload/";

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            File uploadDir = new File(UPLOAD_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            DiskFileItemFactory factory = new DiskFileItemFactory();
            ServletFileUpload upload = new ServletFileUpload(factory);

            List<FileItem> items = upload.parseRequest(request);
            for (FileItem item : items) {
                if (!item.isFormField()) {
                    String originalName = item.getName();
                    if (originalName == null || originalName.isEmpty()) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "文件名为空");
                        return;
                    }

                    //生成唯一文件名
                    String ext = originalName.substring(originalName.lastIndexOf("."));
                    String fileName = UUID.randomUUID().toString() + ext;

                    //构建目标文件路径
                    File file = new File(uploadDir, fileName);
                    item.write(file);

                    //返回给前端的 URL（假设你的域名是 localhost）
                    String mediaUrl = "http://localhost/upload/" + fileName;

                    //返回JSON格式响应
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"url\":\"" + mediaUrl + "\"}");

                    return;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "文件上传失败：" + e.getMessage());
        }
    }
}
