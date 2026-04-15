package dao;

import java.sql.Connection;
import java.sql.DriverManager;

public class dbconnect {
    private static final String URL = "jdbc:mysql://localhost:3307/photobooth";
    private static final String USER = "root";
    private static final String PASS = ""; 

    public static Connection getConnection() throws Exception {
        Class.forName("com.mysql.cj.jdbc.Driver");
        return DriverManager.getConnection(URL, USER, PASS);
    }

public static void main(String[] args) {
        try {
            Connection c = getConnection();
            if (c != null) {
                System.out.println("✅ MANTAP! Database Berhasil Tersambung.");
            }
        } catch (Exception e) {
            System.out.println("❌ GAGAL KONEK: " + e.getMessage());
            e.printStackTrace();
        }
    }
}