package com.example.projet_campus.db;

import com.example.projet_campus.classes.*;
import java.sql.*;
import java.util.*;

public class PersonDAO {
    public static List<Person> getAll() throws SQLException {
        List<Person> out = new ArrayList<>();
        String sql = "SELECT id, name, age, resource_consumption, role, sector, satisfaction, subject, is_available "
                + "FROM person";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                String role = rs.getString("role");
                if ("STUDENT".equalsIgnoreCase(role)) {
                    Student s = new Student(
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getDouble("resource_consumption"),
                            rs.getString("sector"),
                            rs.getInt("satisfaction")
                    );
                    out.add(s);
                } else {
                    Professor p = new Professor(
                            rs.getString("name"),
                            rs.getInt("age"),
                            rs.getDouble("resource_consumption"),
                            null, 0,
                            rs.getString("subject"),
                            rs.getBoolean("is_available")
                    );
                    out.add(p);
                }
            }
        }
        return out;
    }
    public static void deleteByName(String name) throws SQLException {
        String sql = "DELETE FROM person WHERE name = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    public static void insert(Person p) throws SQLException {
        String sql = """
      INSERT INTO person
        (name, age, resource_consumption, role,
         sector, satisfaction, subject, is_available)
      VALUES (?, ?, ?, ?, ?, ?, ?, ?)
    """;
        try (var conn = Database.getConnection();
             var ps   = conn.prepareStatement(sql)) {

            ps.setString(1, p.getName());
            ps.setInt   (2, ((Person)p).age);               // make `age` package‑private or add getter
            ps.setDouble(3, p.getResourceConsumption());

            if (p instanceof Student s) {
                ps.setString (4, "STUDENT");
                ps.setString (5, s.getSector());
                ps.setInt    (6, s.getSatisfaction());
                ps.setNull   (7, Types.VARCHAR);
                ps.setNull   (8, Types.BOOLEAN);
            } else {
                var prof = (Professor)p;
                ps.setString (4, "PROFESSOR");
                ps.setNull   (5, Types.VARCHAR);
                ps.setNull   (6, Types.INTEGER);
                ps.setString (7, prof.subject);
                ps.setBoolean(8, prof.availability);
            }

            ps.executeUpdate();
        }
    }
    public static void update(Person p) throws SQLException {
        String sql = "UPDATE person SET name=?, resource_consumption=? WHERE name=?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getName());
            ps.setDouble(2, p.getResourceConsumption());
            ps.setString(3, /* original name parameter – you may need an ID instead */ p.getName());
            ps.executeUpdate();
        }
    }
}
