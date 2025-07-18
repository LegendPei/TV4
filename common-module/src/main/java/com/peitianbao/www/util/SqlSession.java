package com.peitianbao.www.util;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
public class SqlSession implements AutoCloseable {
    private final ConnectionPool connectionPool;
    private final DataSource dataSource;
    private final Connection connection;
    private final boolean isExternalConnection;

    //用于不使用Seata的模块
    public SqlSession(ConnectionPool connectionPool) throws InterruptedException, SQLException {
        this.connectionPool = connectionPool;
        this.dataSource = null;
        this.connection = connectionPool.getConnection();
        this.isExternalConnection = false;
    }

    //用于支持Seata的模块
    public SqlSession(DataSource dataSource) throws SQLException {
        this.connectionPool = null;
        this.dataSource = dataSource;
        this.connection = dataSource.getConnection();
        this.isExternalConnection = true;
    }

    public int executeUpdate(String sqlId, Map<String, Object> params) throws SQLException {
        String sql = XmlParse.getSql(sqlId);
        if (sql == null) {
            LoggingFramework.severe("执行executeUpdate时未找到对应的sql语句:" + sqlId);
            throw new IllegalArgumentException("未找到对应的sql语句:" + sqlId);
        }

        sql = replacePlaceholders(sql, params);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            return statement.executeUpdate();
        }
    }

    public <T> T executeQueryForObject(String sqlId, Map<String, Object> params, Class<T> clazz) throws Exception {
        String sql = XmlParse.getSql(sqlId);
        if (sql == null) {
            LoggingFramework.severe("执行executeQueryForObject时未找到对应的sql语句:" + sqlId);
            throw new IllegalArgumentException("未找到对应的sql语句：" + sqlId);
        }

        sql = replacePlaceholders(sql, params);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (var field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(obj, resultSet.getObject(field.getName()));
                }
                return obj;
            }
        }
        return null;
    }

    public <T> List<T> executeQueryForList(String sqlId, Map<String, Object> params, Class<T> clazz) throws Exception {
        String sql = XmlParse.getSql(sqlId);
        if (sql == null) {
            LoggingFramework.severe("执行executeQueryForList时未找到对应的sql语句:" + sqlId);
            throw new IllegalArgumentException("未找到对应的sql语句：" + sqlId);
        }

        sql = replacePlaceholders(sql, params);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();

            List<T> resultList = new ArrayList<>();
            while (resultSet.next()) {
                T obj = clazz.getDeclaredConstructor().newInstance();
                for (var field : clazz.getDeclaredFields()) {
                    field.setAccessible(true);
                    field.set(obj, resultSet.getObject(field.getName()));
                }
                resultList.add(obj);
            }
            return resultList;
        }
    }

    public int executeQueryForInt(String sqlId, Map<String, Object> params) throws SQLException {
        String sql = XmlParse.getSql(sqlId);
        if (sql == null) {
            LoggingFramework.severe("执行executeQueryForInt时未找到对应的sql语句:" + sqlId);
            throw new IllegalArgumentException("未找到对应的sql语句：" + sqlId);
        }

        sql = replacePlaceholders(sql, params);

        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1);
            }
            return 0;
        }
    }
    //将占位符替换为实际的参数值
    private String replacePlaceholders(String sql, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof String) {
                sql = sql.replace("#{" + entry.getKey() + "}", "'" + value + "'");
            } else if (value instanceof LocalDateTime) {
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                String formatted = ((LocalDateTime) value).format(formatter);
                sql = sql.replace("#{" + entry.getKey() + "}", "'" + formatted + "'");
            } else {
                sql = sql.replace("#{" + entry.getKey() + "}", value.toString());
            }
        }
        return sql;
    }

    @Override
    public void close() {
        if (connection != null) {
            connectionPool.releaseConnection(connection);
        }
    }
}