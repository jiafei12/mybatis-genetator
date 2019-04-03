package com.github.walker.mybatis.daoj.test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Created by song on 09/10/2017.
 */
public class App
{
    static final String JDBC_DRIVER = "oracle.jdbc.OracleDriver";
    static final String DB_URL = "jdbc:oracle:thin:@192.168.1.71:1521:orcl";

    //  Database credentials
    static final String USER = "hexin_factoring";
    static final String PASS = "hexin_factoring";


    public static void main(String[] aa) throws ClassNotFoundException, SQLException
    {
        Class.forName(JDBC_DRIVER);

        System.out.println("Connecting to database...");
        Connection conn = DriverManager.getConnection(DB_URL,USER,PASS);

        System.out.println("Creating statement...");
        conn.createStatement();
    }
}
