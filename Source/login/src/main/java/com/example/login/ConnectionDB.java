package com.example.login;

import java.sql.Connection;
import java.sql.DriverManager;

public class ConnectionDB {

    // Đổi từ public static void main thành public static Connection
    public static Connection conDB() {
        try {
            // Khai báo driver MySQL
            Class.forName("com.mysql.cj.jdbc.Driver");

            // Thông tin Database của bạn
            String url = "jdbc:mysql://localhost:3306/system";
            String user = "root";
            String pass = "";

            // Thực hiện kết nối
            Connection con = DriverManager.getConnection(url, user, pass);
            System.out.println("Kết nối cơ sở dữ liệu thành công!");
            return con; // Trả về biến kết nối để form Login sử dụng

        } catch (Exception e) {
            System.out.println("Lỗi kết nối cơ sở dữ liệu!");
            e.printStackTrace();
            return null;
        }
    }
}