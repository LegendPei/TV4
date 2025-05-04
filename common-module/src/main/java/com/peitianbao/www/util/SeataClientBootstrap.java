package com.peitianbao.www.util;

import io.seata.config.ConfigurationFactory;
import io.seata.core.model.TransactionManager;
import io.seata.rm.datasource.DataSourceProxy;
import io.seata.tm.DefaultTransactionManager;
import io.seata.tm.TMClient;
import io.seata.tm.TransactionManagerHolder;

import javax.sql.DataSource;

/**
 * @author leg
 */
public class SeataClientBootstrap {

    public static void init(String applicationId, String txServiceGroup) {
        try {
            // 显式加载配置
            ConfigurationFactory.getInstance(); // 触发配置加载

            // 初始化 TM
            TransactionManager tm = new DefaultTransactionManager();
            TransactionManagerHolder.set(tm);

            // 初始化 RM
            io.seata.rm.DefaultResourceManager.get();
            io.seata.rm.RMClient.init(applicationId, txServiceGroup);
            TMClient.init(applicationId, txServiceGroup);
            System.out.println("Seata 客户端初始化成功！");
            System.out.println("应用ID: " + applicationId);
            System.out.println("事务分组: " + txServiceGroup);

        } catch (Exception e) {
            throw new RuntimeException("Seata 客户端初始化失败！", e);
        }
    }

    public static DataSourceProxy wrapDataSource(DataSource originalDataSource) {
        return new DataSourceProxy(originalDataSource);
    }
}