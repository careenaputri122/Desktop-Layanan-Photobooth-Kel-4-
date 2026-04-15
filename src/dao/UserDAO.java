package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

public class UserDAO {

    public static void register(String namaDepan, String namaBelakang, String email, String password) throws Exception {

        Connection conn = DriverManager.getConnection(
            "jdbc:mysql://localhost:3306/db_app",
            "root",
            ""
        );

        String query = "INSERT INTO user (nama_depan, nama_belakang, email, password) VALUES (?, ?, ?, ?)";

        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, namaDepan);
        ps.setString(2, namaBelakang);
        ps.setString(3, email);
        ps.setString(4, password);

        ps.executeUpdate();
        System.out.println("User berhasil didaftarkan!");

        conn.close();
    }
}