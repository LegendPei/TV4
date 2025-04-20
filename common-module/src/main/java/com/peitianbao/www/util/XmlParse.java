package com.peitianbao.www.util;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author leg
 */
public class XmlParse {
    //存放格式如：UserMapper.insertUser：对应sql语句
    private static final Map<String, String> SQL_MAP = new HashMap<>();

    static {
        try {
            //加载XML文件
            Properties properties = LoadProperties.load("application.properties");
            String path = properties.getProperty("mapper.path");
            SAXReader reader = new SAXReader();
            File file = new File(path);
            Document document = reader.read(file);

            //遍历每个mapper节点
            List<Element> mappers = document.getRootElement().elements("mapper");
            for (Element mapper : mappers) {
                String namespace = mapper.attributeValue("namespace");

                //遍历每个sql操作节点
                List<Element> operations = mapper.elements();
                for (Element operation : operations) {
                    String id = operation.attributeValue("id");
                    String fullId = namespace + "." + id;
                    String sql = operation.getTextTrim();
                    SQL_MAP.put(fullId, sql);
                }
            }
        } catch (Exception e) {
            LoggingFramework.severe("XML文件解析失败");
            throw new RuntimeException("XML文件解析失败", e);
        }
    }

    public static String getSql(String fullId) {
        return SQL_MAP.get(fullId);
    }
}
