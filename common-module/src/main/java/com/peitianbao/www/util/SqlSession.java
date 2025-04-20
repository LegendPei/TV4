package com.peitianbao.www.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author leg
 */
public class SqlSession implements AutoCloseable {
    private final ConnectionPool connectionPool;
    private final Connection connection;

    public SqlSession(ConnectionPool connectionPool) throws InterruptedException, SQLException {
        this.connectionPool = connectionPool;
        this.connection = connectionPool.getConnection();
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

    //将占位符替换为实际的参数值
    private String replacePlaceholders(String sql, Map<String, Object> params) {
        for (Map.Entry<String, Object> entry : params.entrySet()) {
            String value = entry.getValue() instanceof String
                    ? "'" + entry.getValue() + "'"
                    : entry.getValue().toString();
            sql = sql.replace("#{" + entry.getKey() + "}", value);
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