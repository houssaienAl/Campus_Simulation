package com.example.projet_campus.db;

import com.example.projet_campus.classes.User;

import java.sql.*;

public class UserDAO {

    public static void register(String username, String passwordHash, String role)
            throws SQLException {
        String sql = "INSERT INTO users (username,password,role) VALUES (?,?,?)";
        try (var conn = Database.getConnection();
             var ps   = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ps.setString(2, passwordHash);
            ps.setString(3, role);
            ps.executeUpdate();
        }
    }

    public static User findByUsername(String username) throws SQLException {
        String sql = "SELECT * FROM users WHERE username = ?";
        try (var conn = Database.getConnection();
             var ps   = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            try (var rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new User(
                        rs.getInt("id"),
                        rs.getString("username"),
                        rs.getString("password"),
                        rs.getString("role")
                );
            }
        }
    }
}
