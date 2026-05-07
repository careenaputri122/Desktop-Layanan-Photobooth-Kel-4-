package dao;

import model.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * [REFAKTOR OOP] UserDAO — extends BaseDao dan implements IDao<User>.
 *
 * Konsep OOP yang diterapkan:
 *  - INHERITANCE  : extends BaseDao → dapat getConnection() tanpa hardcode ulang
 *  - POLYMORPHISM : implements IDao<User> → bisa diperlakukan sebagai IDao di mana pun
 *  - ENCAPSULATION: currentUser tetap private, diakses lewat getter
 *  - SINGLETON    : satu instance shared untuk seluruh aplikasi
 */
public class UserDAO extends BaseDao implements IDao<User> {

    private static final String ROLE_ADMIN = "admin";
    private static final String ROLE_MEMBER = "member";
    private static final String ROLE_USER = "user";

    // ── Singleton ─────────────────────────────────────────────────────────
    private static UserDAO instance;
    private User currentUser;

    private UserDAO() {}

    public static UserDAO getInstance() {
        if (instance == null) instance = new UserDAO();
        return instance;
    }

    // ── Hashing password (MD5 sederhana) ──────────────────────────────────
    private String hashPassword(String plain) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(plain.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return plain;
        }
    }

    // ── Implementasi IDao ─────────────────────────────────────────────────

    @Override
    public boolean save(User user) {
        String sql = "INSERT INTO users (namaDepan, namaBelakang, email, password, role) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNamaDepan());
            ps.setString(2, user.getNamaBelakang());
            ps.setString(3, user.getEmail());
            ps.setString(4, hashPassword(user.getPassword()));
            ps.setString(5, user.getRole());
            ps.executeUpdate();
            return true;

        } catch (SQLIntegrityConstraintViolationException e) {
            return false; // email sudah terdaftar
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Alias agar kode lama yang memanggil register() tetap bekerja. */
    public boolean register(User user) {
        return save(user);
    }

    @Override
    public boolean update(User user) {
        String sql = "UPDATE users SET namaDepan=?, namaBelakang=?, email=?, role=? WHERE id=?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, user.getNamaDepan());
            ps.setString(2, user.getNamaBelakang());
            ps.setString(3, user.getEmail());
            ps.setString(4, user.getRole());
            ps.setInt   (5, user.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public User findById(int id) {
        String sql = "SELECT * FROM users WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public List<User> findAll() {
        List<User> list = new ArrayList<>();
        String sql = "SELECT * FROM users";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    // ── Login / Session ───────────────────────────────────────────────────

    public User login(String email, String password) {
        String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, hashPassword(password));
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                currentUser = mapRow(rs);
                refreshMemberStatus(currentUser);
                return currentUser;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    public User getCurrentUser() {
        refreshMemberStatus(currentUser);
        return currentUser;
    }

    public boolean currentUserHasMemberDiscount() {
        User user = getCurrentUser();
        return user != null && ROLE_MEMBER.equalsIgnoreCase(user.getRole());
    }

    public void refreshMemberStatusById(int userId) {
        if (userId <= 0) return;
        refreshMemberStatus(findById(userId));
    }

    public void refreshMemberStatus(User user) {
        if (user == null || ROLE_ADMIN.equalsIgnoreCase(user.getRole())) return;

        String targetRole = BookingDAO.getInstance().isMemberEligible(user.getId())
            ? ROLE_MEMBER
            : ROLE_USER;

        if (!targetRole.equalsIgnoreCase(user.getRole())) {
            if (updateRole(user.getId(), targetRole)) {
                user.setRole(targetRole);
            }
        }

        if (currentUser != null && currentUser.getId() == user.getId()) {
            currentUser.setRole(user.getRole());
        }
    }

    public void logout()         { currentUser = null; }

    // ── Helper ────────────────────────────────────────────────────────────

    private boolean updateRole(int userId, String role) {
        String sql = "UPDATE users SET role = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, role);
            ps.setInt(2, userId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    private User mapRow(ResultSet rs) throws SQLException {
        return new User(
            rs.getInt   ("id"),
            rs.getString("namaDepan"),
            rs.getString("namaBelakang"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role")
        );
    }
}
