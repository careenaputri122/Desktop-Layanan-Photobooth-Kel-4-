package dao;

import model.Paket;
import java.sql.*;
import java.util.*;

/**
 * PaketDAO — mengelola data paket photobooth ke database.
 *
 * Konsep OOP yang diterapkan:
 *  - INHERITANCE  : extends BaseDao → tidak perlu hardcode koneksi DB lagi
 *  - POLYMORPHISM : implements IDao<Paket> → bisa diperlakukan seragam
 *  - SINGLETON    : satu instance shared untuk seluruh aplikasi
 *
 * Kolom tabel `paket`: id, nama, harga (INT), tipe
 */
public class PaketDAO extends BaseDao implements IDao<Paket> {

    // ── Singleton ─────────────────────────────────────────────────────────
    private static PaketDAO instance;

    private PaketDAO() {}

    public static PaketDAO getInstance() {
        if (instance == null) instance = new PaketDAO();
        return instance;
    }

    // ── Implementasi IDao ─────────────────────────────────────────────────

    @Override
    public boolean save(Paket p) {
        String sql = "INSERT INTO paket (nama, harga, tipe) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNama());
            ps.setInt   (2, p.getHarga());
            ps.setString(3, p.getTipe());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) p.setId(keys.getInt(1));
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public Paket findById(int id) {
        String sql = "SELECT * FROM paket WHERE id = ?";

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
    public List<Paket> findAll() {
        List<Paket> list = new ArrayList<>();
        String sql = "SELECT * FROM paket";

        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean update(Paket p) {
        String sql = "UPDATE paket SET nama=?, harga=?, tipe=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNama());
            ps.setInt   (2, p.getHarga());
            ps.setString(3, p.getTipe());
            ps.setInt   (4, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM paket WHERE id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Paket mapRow(ResultSet rs) throws SQLException {
        Paket p = new Paket();
        p.setId   (rs.getInt   ("id"));
        p.setNama (rs.getString("nama"));
        p.setHarga(rs.getInt   ("harga"));
        p.setTipe (rs.getString("tipe"));
        return p;
    }
}