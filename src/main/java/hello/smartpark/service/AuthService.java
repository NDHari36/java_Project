/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.service;

import org.mindrot.jbcrypt.BCrypt;
import hello.smartpark.dao.UserDAO;
import hello.smartpark.model.User;

import java.sql.SQLException;

/**
 *
 * @author PC
 */
public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public User login(String username, String password) throws SQLException {
        // Kiểm tra dữ liệu được nhập
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập tên đăng nhập!"
            );
        }
        if (password == null || password.isEmpty()) {
            throw new IllegalArgumentException(
                    "Vui lòng nhập mật khẩu!"
            );
        }

        // Tìm tài khoản
        User user = userDAO.findByUsername(username.trim());

        if (user == null) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập hoặc mật khẩu không chính xác!"
            );
        }

        // Kiểm tra mật khẩu BCrypt
        boolean matKhauDung;

        try {
            matKhauDung = BCrypt.checkpw(
                    password,
                    user.getPasswordHash()
            );
        } catch (IllegalArgumentException e) {
            matKhauDung = false;
        }

        if (!matKhauDung) {
            throw new IllegalArgumentException(
                    "Tên đăng nhập hoặc mật khẩu không chính xác!"
            );
        }

        // Kiểm tra trạng thái tài khoản
        if (!"ACTIVE".equals(user.getStatus())) {
            throw new IllegalArgumentException(
                    "Tài khoản đã bị khóa!"
            );
        }

        return user;
    }
}
