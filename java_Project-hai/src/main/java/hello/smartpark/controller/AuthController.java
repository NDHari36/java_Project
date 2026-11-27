/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.controller;

import hello.smartpark.model.User;
import hello.smartpark.service.AuthService;
import hello.smartpark.session.UserSession;
import hello.smartpark.view.auth.LoginFrame;
import hello.smartpark.view.MainFrame;

import java.sql.SQLException;
/**
 *
 * @author PC
 */

public class AuthController {

    private final LoginFrame view;
    private final AuthService authService;

    public AuthController(LoginFrame view) {
        this.view = view;
        this.authService = new AuthService();
    }

    public void handleLogin() {

        // Lấy dữ liệu từ giao diện
        String username = view.getUsername();
        String password = view.getPassword();

        try {

            // Gọi xử lý đăng nhập
            User user = authService.login(username, password);

            // Lưu phiên đăng nhập
            UserSession.setCurrentUser(user);

            // Mở giao diện chính
            MainFrame mainFrame = new MainFrame();
            mainFrame.setVisible(true);

            // Đóng cửa sổ đăng nhập
            view.dispose();

        } catch (IllegalArgumentException  e) {

            // Lỗi thông tin đăng nhập
            view.thongBao(e.getMessage());
            view.clearMK();

        } catch (SQLException e) {

            // Lỗi kết nối hoặc truy vấn database
            e.printStackTrace();

            view.thongBao(
                "Không thể kết nối cơ sở dữ liệu. "
                + "Vui lòng kiểm tra lại!"
            );
        }
    }
}
