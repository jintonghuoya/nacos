/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.nacos.config.server.service;

import com.alibaba.nacos.config.server.constant.Constants;
import com.alibaba.nacos.config.server.utils.LogUtil;
import com.alibaba.nacos.config.server.utils.PropertyUtil;
import com.alibaba.nacos.core.utils.DiskUtils;
import com.zaxxer.hikari.HikariDataSource;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import static com.alibaba.nacos.core.utils.SystemUtils.NACOS_HOME;
import static com.alibaba.nacos.core.utils.SystemUtils.NACOS_HOME_KEY;

/**
 * local data source
 *
 * @author Nacos
 */
@Conditional(ConditionOnUseDerby.class)
@DependsOn(value = "serverMemberManager")
@Component("localDataSourceService")
public class LocalDataSourceServiceImpl implements DataSourceService {

    private static final String JDBC_DRIVER_NAME = "org.apache.derby.jdbc.EmbeddedDriver";
    private static final String USER_NAME = "nacos";
    private static final String PASSWORD = "nacos";
    private static final String DERBY_BASE_DIR = "data" + File.separator + "derby-data";

    private volatile JdbcTemplate jt;
    private volatile TransactionTemplate tjt;

    private boolean initialize = false;
    private boolean jdbcTemplateInit = false;

    @PostConstruct
    @Override
    public synchronized void init() throws Exception {
        if (!PropertyUtil.isUseMysql()) {
            if (!initialize) {
                LogUtil.defaultLog.info("use local db service for init");

                final String jdbcUrl = "jdbc:derby:" + Paths.get(NACOS_HOME, DERBY_BASE_DIR).toString() + ";create=true";

                initialize(jdbcUrl);
                initialize = true;
            }
        }
    }

    @Override
    public synchronized void reload() {
        DataSource ds = jt.getDataSource();
        if (ds == null) {
            throw new RuntimeException("datasource is null");
        }
        try {
            execute(ds.getConnection(), "META-INF/schema.sql");
        } catch (Exception e) {
            if (LogUtil.defaultLog.isErrorEnabled()) {
                LogUtil.defaultLog.error(e.getMessage(), e);
            }
            throw new RuntimeException("load schema.sql error.", e);
        }
    }

    public void reopenDerby(String jdbcUrl, Callable<Void> callable) throws Exception {
        if (!PropertyUtil.isUseMysql()) {

            LogUtil.defaultLog.warn("use local db service for reopenDerby");
            try {
                DriverManager.getConnection("jdbc:derby:;shutdown=true");
            } catch (Exception e) {

                // An error is thrown when the Derby shutdown is executed, which should be ignored

                if (!StringUtils.contains(e.getMessage().toLowerCase(), "Derby system shutdown.".toLowerCase())) {
                    throw e;
                }
            }
            DiskUtils.deleteDirectory(Paths.get(NACOS_HOME, DERBY_BASE_DIR).toString());
            callable.call();
            initialize(jdbcUrl);
        }
    }

    private synchronized void initialize(String jdbcUrl) {
        HikariDataSource ds = new HikariDataSource();
        ds.setDriverClassName(JDBC_DRIVER_NAME);
        ds.setJdbcUrl(jdbcUrl);
        ds.setUsername(USER_NAME);
        ds.setPassword(PASSWORD);
        ds.setMaximumPoolSize(80);
        ds.setConnectionTimeout(10000L);
        DataSourceTransactionManager tm = new DataSourceTransactionManager();
        tm.setDataSource(ds);

        if (jdbcTemplateInit) {
            jt.setDataSource(ds);
            tjt.setTransactionManager(tm);
        } else {
            jt = new JdbcTemplate();
            jt.setMaxRows(50000);
            jt.setQueryTimeout(5000);
            jt.setDataSource(ds);
            tjt = new TransactionTemplate(tm);
            tjt.setTimeout(5000);
            jdbcTemplateInit = true;
        }
        reload();
    }

    @Override
    public boolean checkMasterWritable() {
        return true;
    }

    @Override
    public JdbcTemplate getJdbcTemplate() {
        return jt;
    }

    @Override
    public TransactionTemplate getTransactionTemplate() {
        return tjt;
    }

    @Override
    public String getCurrentDBUrl() {
        return "jdbc:derby:" + NACOS_HOME + File.separator + DERBY_BASE_DIR + ";create=true";
    }

    @Override
    public String getHealth() {
        return "UP";
    }

    /**
     * 读取SQL文件
     *
     * @param sqlFile sql
     * @return sqls
     * @throws Exception Exception
     */
    private List<String> loadSql(String sqlFile) throws Exception {
        List<String> sqlList = new ArrayList<String>();
        InputStream sqlFileIn = null;
        try {
            if (StringUtils.isBlank(System.getProperty(NACOS_HOME_KEY))) {
                ClassLoader classLoader = getClass().getClassLoader();
                URL url = classLoader.getResource(sqlFile);
                sqlFileIn = url.openStream();
            } else {
                File file = new File(
                        System.getProperty(NACOS_HOME_KEY) + File.separator + "conf" + File.separator + "schema.sql");
                sqlFileIn = new FileInputStream(file);
            }

            StringBuilder sqlSb = new StringBuilder();
            byte[] buff = new byte[1024];
            int byteRead = 0;
            while ((byteRead = sqlFileIn.read(buff)) != -1) {
                sqlSb.append(new String(buff, 0, byteRead, Constants.ENCODE));
            }

            String[] sqlArr = sqlSb.toString().split(";");
            for (int i = 0; i < sqlArr.length; i++) {
                String sql = sqlArr[i].replaceAll("--.*", "").trim();
                if (StringUtils.isNotEmpty(sql)) {
                    sqlList.add(sql);
                }
            }
            return sqlList;
        } catch (Exception ex) {
            throw new Exception(ex.getMessage());
        } finally {
            if (sqlFileIn != null) {
                sqlFileIn.close();
            }
        }
    }

    /**
     * 执行SQL语句
     *
     * @param conn    connect
     * @param sqlFile sql
     * @throws Exception Exception
     */
    private void execute(Connection conn, String sqlFile) throws Exception {
        Statement stmt = null;
        try {
            List<String> sqlList = loadSql(sqlFile);
            stmt = conn.createStatement();
            for (String sql : sqlList) {
                try {
                    stmt.execute(sql);
                } catch (Exception e) {
                    LogUtil.defaultLog.warn(e.getMessage());
                }
            }
        } finally {
            if (stmt != null) {
                stmt.close();
            }
        }
    }

}
