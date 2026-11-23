/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.dao;

import hello.smartpark.model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author PC
 */
public class UserDAO {

    public User findByUsername(String username) throws SQLException {
        Connection conn = JDBCConnection.getConnection();
        String sql = """
            SELECT u.user_id, u.username, u.password_hash, 
            u.full_name, u.phone, u.role_id, r.role_name, u.status
            FROM users u INNER JOIN roles r  ON u.role_id = r.role_id  WHERE u.username = ?
            """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, username);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setPasswordHash(rs.getString("password_hash"));
                user.setFullName(rs.getString("full_name"));
                user.setPhone(rs.getString("phone"));
                user.setRoleId(rs.getInt("role_id"));
                user.setRoleName(rs.getString("role_name"));
                user.setStatus(rs.getString("status"));
                return user;
            }
        }
        return null;
    }

    public List<User> getEmployeesForAssignment() throws SQLException {
        List<User> danhSach = new ArrayList<>();

        String sql = """
        SELECT *
        FROM users 
        WHERE role_id = 2
        ORDER BY full_name
        """;

        try (Connection conn = JDBCConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                User user = new User();
                user.setUserId(rs.getInt("user_id"));
                user.setUsername(rs.getString("username"));
                user.setFullName(rs.getString("full_name"));
                user.setPhone(rs.getString("phone"));

                danhSach.add(user);
            }
        }
        return danhSach;
    }
}
