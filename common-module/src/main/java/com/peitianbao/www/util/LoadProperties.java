package com.peitianbao.www.util;

import java.io.InputStream;
import java.util.Properties;

/**
 * @author leg
 */
public class LoadProperties {
    public static Properties load(String fileName) {
        Properties properties = new Properties();
        try (InputStream input = LoadProperties.class.getClassLoader().getResourceAsStream(fileName)) {
            if (input == null) {
                throw new RuntimeException("无法找到配置文件：" + fileName);
            }
            properties.load(input);
        } catch (Exception e) {
            throw new RuntimeException("加载配置文件失败: " + fileName, e);
        }
        return properties;
    }
}
