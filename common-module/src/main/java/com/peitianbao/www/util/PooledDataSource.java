package com.peitianbao.www.util;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * @author leg
 */
public class PooledDataSource implements DataSource {

    private final ConnectionPool connectionPool;

    public PooledDataSource(ConnectionPool connectionPool) {
        this.connectionPool = connectionPool;
    }

    @Override
    public Connection getConnection() throws SQLException {
        try {
            return connectionPool.getConnection();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("获取数据库连接时线程中断", e);
        }
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return getConnection();
    }

    @Override
    public PrintWriter getLogWriter() {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out)  {
        // ignore
    }

    @Override
    public void setLoginTimeout(int seconds)  {
        // ignore
    }

    @Override
    public int getLoginTimeout() {
        return 0;
    }

    @Override
    public Logger getParentLogger() {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface){
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface){
        return false;
    }
}