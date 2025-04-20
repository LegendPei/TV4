package com.peitianbao.www.mybatis;

import com.peitianbao.www.util.ConnectionPool;
import com.peitianbao.www.util.LoggingFramework;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * @author leg
 */
public class MapperProxy implements InvocationHandler {

    private final Class<?> entityClass;
    private final ConnectionPool connectionPool;

    public MapperProxy(Class<?> entityClass, ConnectionPool connectionPool) {
        this.entityClass = entityClass;
        this.connectionPool = connectionPool;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        String methodName = method.getName();

        String sql;
        switch (methodName) {
            case "insert" -> sql = SqlGenerator.generateInsertSql(entityClass);
            case "deleteById" -> sql = SqlGenerator.generateDeleteByIdSql(entityClass);
            case "update" -> sql = SqlGenerator.generateUpdateSql(entityClass);
            case "selectById" -> sql = SqlGenerator.generateSelectByIdSql(entityClass);
            case "selectAll" -> sql = SqlGenerator.generateSelectAllSql(entityClass);
            default -> throw new RuntimeException("Unsupported method: " + methodName);
        }

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet resultSet = null;

        try {
            // 从连接池获取连接
            connection = connectionPool.getConnection();
            preparedStatement = connection.prepareStatement(sql);

            // 设置参数
            if (args != null) {
                for (int i = 0; i < args.length; i++) {
                    preparedStatement.setObject(i + 1, args[i]);
                }
            }

            // 执行 SQL
            if (methodName.startsWith("select")) {
                resultSet = preparedStatement.executeQuery();
                // TODO: 将 ResultSet 转换为实体对象或列表
                return null;
            } else {
                return preparedStatement.executeUpdate();
            }
        } catch (Exception e) {
            throw new RuntimeException("SQL execution failed", e);
        } finally {
            // 释放资源
            if (resultSet != null) {
                try {
                    resultSet.close();
                } catch (Exception e) {
                    LoggingFramework.logException(e);
                }
            }
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (Exception e) {
                    LoggingFramework.logException(e);
                }
            }
            if (connection != null) {
                connectionPool.releaseConnection(connection);
            }
        }
    }
}