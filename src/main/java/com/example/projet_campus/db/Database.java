package com.example.projet_campus.db;

import java.sql.*;

public class Database {
    private static final String URL      =
            "jdbc:mysql://localhost:3306/university?useSSL=false&serverTimezone=UTC";
    private static final String USER     = "root";
    private static final String PASSWORD = "";  // your WAMP MySQL password

    static {
        try { Class.forName("com.mysql.cj.jdbc.Driver"); }
        catch (ClassNotFoundException e) {
            throw new RuntimeException("MySQL driver missing", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
