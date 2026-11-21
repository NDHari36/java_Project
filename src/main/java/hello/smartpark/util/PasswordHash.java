/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package hello.smartpark.util;

import org.mindrot.jbcrypt.BCrypt;

/**
 *
 * @author PC
 */
public class PasswordHash {

    public static void main(String[] args) {

        String matKhau = "Admin@123";

        String matKhauDaMaHoa = BCrypt.hashpw(
                matKhau,
                BCrypt.gensalt(12)
        );

        System.out.println("Mat khau da ma hoa:");
        System.out.println(matKhauDaMaHoa);
    }
}
