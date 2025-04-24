package com.example.projet_campus.db;

import com.example.projet_campus.classes.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BuildingDAO {

    /**
     * Fetches every row from `building` and turns it into the proper Building subclass.
     */
    public static List<Building> getAll() throws SQLException {
        String sql = """
            SELECT 
              id, name, type, capacity, resource_consumption, satisfaction_impact 
            FROM building
            """;

        List<Building> out = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery())
        {
            while (rs.next()) {
                int    id   = rs.getInt("id");
                String name = rs.getString("name");
                String type = rs.getString("type").toLowerCase();
                int    cap  = rs.getInt("capacity");
                double rc   = rs.getDouble("resource_consumption");
                int    si   = rs.getInt("satisfaction_impact");

                Building b;
                switch (type) {
                    case "library"    -> b = new Library(id, name, cap);
                    case "classroom"  -> b = new Classroom(id, name, cap);
                    case "laboratory" -> b = new Laboratory(id, name, cap);
                    case "cafeteria"  -> b = new Cafeteria(id, name, cap);
                    default           -> b = new Building(id, name, type, cap, rc, si);
                }
                out.add(b);
            }
        }
        return out;
    }

    /** Deletes by name. */
    public static void deleteByName(String name) throws SQLException {
        String sql = "DELETE FROM building WHERE name = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.executeUpdate();
        }
    }

    /** Inserts a new building. */
    public static void insert(Building b) throws SQLException {
        String sql = """
            INSERT INTO building
              (name, type, capacity, resource_consumption, satisfaction_impact)
            VALUES (?, ?, ?, ?, ?)
            """;
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS))
        {
            ps.setString(1, b.getName());
            ps.setString(2, b.getType());
            ps.setInt   (3, b.getCapacity());
            ps.setDouble(4, b.getResourceConsumption());
            ps.setInt   (5, b.getSatisfactionImpact());
            ps.executeUpdate();

            // if you want to read back and set the auto‐generated id:
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (keys.next()) {
                    b.setId(keys.getInt(1));
                }
            }
        }
    }

    /** Updates name & capacity by id. */
    public static void update(Building b) throws SQLException {
        String sql = "UPDATE building SET name = ?, capacity = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql))
        {
            ps.setString(1, b.getName());
            ps.setInt   (2, b.getCapacity());
            ps.setInt   (3, b.getId());
            ps.executeUpdate();
        }
    }
}
