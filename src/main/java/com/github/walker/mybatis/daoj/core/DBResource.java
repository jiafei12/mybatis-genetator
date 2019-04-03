package com.github.walker.mybatis.daoj.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;


class DBResource
{

    private static Logger log = LoggerFactory.getLogger(DBResource.class);

    protected static Connection getConnection() throws Exception
    {
        String driverStr = ConfigLoader.getJdbcDriver();
        String dataSource = ConfigLoader.getJdbcUrl();
        String username = ConfigLoader.getUsername();
        String password = ConfigLoader.getPassword();

        Properties props = new Properties();
        props.put("remarksReporting","true"); // 不加这个ORACLE无法获取字段注释
        props.put("user",username);
        props.put("password",password);
        props.setProperty("oracle.jdbc.V8Compatible","true");

        Class.forName(driverStr);

        return java.sql.DriverManager.getConnection(dataSource, props);
    }


    protected static void freeConnection(Connection conn) throws Exception
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            log.error(e.getMessage(), e);
        }
    }
}
