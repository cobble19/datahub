package com.linkedin.metadata.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Properties;

public class DBChecker
{
    public static void main(String[] args) {
        connection(args[0], args[1], args[2], args[3]);
    }

    public static void connection(String user, String password, String driver, String url) {

        Properties props = new Properties();
        props.setProperty("user", user);
        props.setProperty("password", password);

        Connection conn = null;
        try {
            ClassLoader contextLoader = Thread.currentThread().getContextClassLoader();
            if (contextLoader != null) {
                Class.forName(driver, true, contextLoader);
            } else {
                Class.forName(driver, true, DBChecker.class.getClassLoader());
            }
            conn = DriverManager.getConnection(url, props);
            PreparedStatement ps = conn.prepareStatement("select 1");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                System.out.println("Executing 'select 1': " + rs.getString(1));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            try {
                if(conn != null) {
                    conn.close();
                }
            } catch (Exception ignored){}
        }

    }
}