package com.peitianbao.www.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author leg
 */
public class ConnectionPool implements AutoCloseable {

    private final String databaseUrl;
    private final String user;
    private final String password;
    private final BlockingQueue<Connection> connectionPool;
    private boolean isClosed = false;

    public ConnectionPool(String configFileName) {
        try {
            Properties properties = LoadProperties.load(configFileName);

            this.databaseUrl = properties.getProperty("database.url");
            this.user = properties.getProperty("database.user");
            this.password = properties.getProperty("database.password");
            int poolSize = Integer.parseInt(properties.getProperty("database.pool.size", "5"));

            this.connectionPool = new LinkedBlockingQueue<>(poolSize);
            for (int i = 0; i < poolSize; i++) {
                Connection connection = createConnection();
                connectionPool.add(connection);
            }
        } catch (Exception e) {
            throw new RuntimeException("连接池初始化失败", e);
        }
    }

    private Connection createConnection() throws SQLException {
        try {
            //显式加载MySQL驱动
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("未找到 MySQL JDBC 驱动", e);
        }
        return DriverManager.getConnection(databaseUrl, user, password);
    }

    public Connection getConnection() throws InterruptedException, SQLException {
        if (isClosed) {
            throw new IllegalStateException("连接池已关闭");
        }

        Connection connection = connectionPool.poll(5, TimeUnit.SECONDS);
        if (connection == null) {
            throw new SQLException("无法从连接池获取连接：超时");
        }

        if (!isValid(connection)) {
            try {
                connection.close();
            } catch (SQLException e) {
                LoggingFramework.severe("关闭无效连接时出错: " + e.getMessage());
                LoggingFramework.logException(e);
            }

            Connection newConnection = createConnection();
            connectionPool.add(newConnection);
            return newConnection;
        }

        return connection;
    }

    public void releaseConnection(Connection connection) {
        if (connection == null) {
            LoggingFramework.warning("尝试释放null连接");
            return;
        }

        if (isClosed) {
            LoggingFramework.warning("尝试在连接池关闭后释放连接:" + connection);
            try {
                if (!connection.isClosed()) {
                    connection.close();
                    LoggingFramework.info("连接池关闭后关闭泄漏连接:" + connection);
                }
            } catch (SQLException e) {
                LoggingFramework.severe("关闭泄漏的连接时出错:" + e.getMessage());
                LoggingFramework.logException(e);
            }
            return;
        }

        try {
            if (!connection.isClosed()) {
                boolean added = connectionPool.offer(connection, 5, TimeUnit.SECONDS);
                if (!added) {
                    LoggingFramework.warning("无法将连接返回到连接池：队列已满。关闭的连接池：" + connection);
                    try {
                        connection.close();
                    } catch (SQLException e) {
                        LoggingFramework.severe("关闭泄漏的连接时出错：" + e.getMessage());
                        LoggingFramework.logException(e);
                    }
                } else {
                    LoggingFramework.info("将连接归还到连接池: " + connection);
                }
            } else {
                LoggingFramework.warning("尝试释放已关闭的连接: " + connection);
            }
        } catch (InterruptedException e) {
            LoggingFramework.severe("释放连接时线程中断: " + e.getMessage());
            Thread.currentThread().interrupt();
        } catch (SQLException e) {
            LoggingFramework.severe("释放连接时出错: " + e.getMessage());
            LoggingFramework.logException(e);
        }
    }

    private boolean isValid(Connection connection) {
        try {
            return connection != null && !connection.isClosed() && connection.isValid(5);
        } catch (SQLException e) {
            return false;
        }
    }

    @Override
    public void close() {
        if (isClosed) {
            LoggingFramework.info("连接池已关闭");
            return;
        }

        LoggingFramework.info("关闭所有连接:");
        for (Connection connection : connectionPool) {
            try {
                if (connection != null && !connection.isClosed()) {
                    LoggingFramework.info("关闭连接: " + connection);
                    connection.close();
                }
            } catch (SQLException e) {
                LoggingFramework.severe("关闭连接时出错: " + e.getMessage());
                LoggingFramework.logException(e);
            }
        }

        connectionPool.clear();
        isClosed = true;
        LoggingFramework.info("连接池已成功关闭");
    }
}