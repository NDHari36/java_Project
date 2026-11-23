/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.service;

import hello.smartpark.dao.UserDAO;
import hello.smartpark.model.User;
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author PC
 */
public class UserService {

    private final UserDAO userDAO = new UserDAO();

    public List<User> getEmployeesForAssignment() throws SQLException {
        return userDAO.getEmployeesForAssignment();
    }

}
