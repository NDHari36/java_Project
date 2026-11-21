/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.session;

import hello.smartpark.model.User;

/**
 *
 * @author PC
 */
public class UserSession {
     private static User currentUser;

    // Lưu tài khoản đăng nhập
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    // Lấy tài khoản hiện tại
    public static User getCurrentUser() {
        return currentUser;
    }

    // Kiểm tra đã đăng nhập chưa
    public static boolean isLoggedIn() {
        return currentUser != null;
    }

    // Đăng xuất
    public static void clearSession() {
        currentUser = null;
    }
}

