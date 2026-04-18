package dao;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    private static UserDAO instance;
    private User currentUser;

    private static final String URL  = "jdbc:mysql://localhost:3306/photobooth_db";
    private static final String USER = "root";
    private static final String PASS = "";

    private UserDAO() {}

    public static UserDAO getInstance() {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    private Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASS);
    }

    public boolean register(User user) {
        String sql = "INSERT INTO users (namaDepan, namaBelakang, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNamaDepan());
            ps.setString(2, user.getNamaBelakang());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getPassword());
            ps.setString(5, user.getRole());
            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            return false;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUser = new User(
                    rs.getInt("id"),
                    rs.getString("namaDepan"),
                    rs.getString("namaBelakang"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role")
                );
                return currentUser;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getCurrentUser() { return currentUser; }
    public void logout()         { currentUser = null; }

    public List<User> getAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) {
                list.add(new User(
                    rs.getInt("id"),
                    rs.getString("namaDepan"),
                    rs.getString("namaBelakang"),
                    rs.getString("email"),
                    rs.getString("password"),
                    rs.getString("role")
                ));
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}