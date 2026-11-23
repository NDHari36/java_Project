/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.controller;

import hello.smartpark.model.User;
import hello.smartpark.service.UserService;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author PC
 */
public class UserController {
    private final UserService userService = new UserService();

    // Lấy danh sách nhân viên để phân công ca
    public List<User> getEmployeesForAssignment() throws SQLException {
        return userService.getEmployeesForAssignment();
    }
}
