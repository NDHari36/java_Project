/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package hello.smartpark.dao;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
/**
 *
 * @author PC
 */

public class JDBCConnection {

    private static final String URL =
        "jdbc:mysql://localhost:3306/smartpark";

    private static final String USER = "root";
    private static final String PASSWORD = "";

    public static Connection getConnection() {
        try {
            Connection conn = DriverManager.getConnection(
                URL, USER, PASSWORD
            );
            
            System.out.println("Ket noi database thanh cong!");
            return conn;

        } catch (SQLException e) {
            System.out.println("Loi ket noi: " + e.getMessage());
            return null;
        }
    }
}
