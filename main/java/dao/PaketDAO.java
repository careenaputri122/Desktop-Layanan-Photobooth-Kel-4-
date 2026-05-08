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
 * Kolom tabel `paket`: id, nama, harga (INT), tipe, keterangan, diskon_member, promo_umum, jam_operasional
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
        ensurePaketColumns();
        String sql = "INSERT INTO paket (nama, harga, tipe, keterangan, diskon_member, promo_umum, jam_operasional) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, p.getNama());
            ps.setInt   (2, p.getHarga());
            ps.setString(3, p.getTipe());
            ps.setString(4, p.getKeterangan());
            ps.setInt   (5, p.getDiskonMember());
            ps.setInt   (6, p.getPromoUmum());
            ps.setString(7, p.getJamOperasional());

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
        ensurePaketColumns();
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
        ensurePaketColumns();
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
        ensurePaketColumns();
        String sql = "UPDATE paket SET nama=?, harga=?, tipe=?, keterangan=?, diskon_member=?, promo_umum=?, jam_operasional=? WHERE id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, p.getNama());
            ps.setInt   (2, p.getHarga());
            ps.setString(3, p.getTipe());
            ps.setString(4, p.getKeterangan());
            ps.setInt   (5, p.getDiskonMember());
            ps.setInt   (6, p.getPromoUmum());
            ps.setString(7, p.getJamOperasional());
            ps.setInt   (8, p.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean delete(int id) {
        ensurePaketColumns();
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
        p.setId        (rs.getInt   ("id"));
        p.setNama      (rs.getString("nama"));
        p.setHarga     (rs.getInt   ("harga"));
        p.setTipe      (rs.getString("tipe"));
        p.setKeterangan(rs.getString("keterangan"));
        p.setDiskonMember(rs.getInt("diskon_member"));
        p.setPromoUmum(rs.getInt("promo_umum"));
        p.setJamOperasional(rs.getString("jam_operasional"));
        return p;
    }

    private void ensurePaketColumns() {
        try (Connection conn = getConnection()) {
            if (!columnExists(conn, "paket", "keterangan")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE paket ADD COLUMN keterangan TEXT NULL AFTER tipe");
                }
            }
            if (!columnExists(conn, "paket", "diskon_member")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE paket ADD COLUMN diskon_member INT NOT NULL DEFAULT 0 AFTER keterangan");
                }
            }
            if (!columnExists(conn, "paket", "promo_umum")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE paket ADD COLUMN promo_umum INT NOT NULL DEFAULT 0 AFTER diskon_member");
                }
            }
            if (!columnExists(conn, "paket", "jam_operasional")) {
                try (Statement st = conn.createStatement()) {
                    st.executeUpdate("ALTER TABLE paket ADD COLUMN jam_operasional VARCHAR(50) NULL AFTER promo_umum");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean columnExists(Connection conn, String tableName, String columnName) throws SQLException {
        DatabaseMetaData meta = conn.getMetaData();
        try (ResultSet rs = meta.getColumns(conn.getCatalog(), null, tableName, columnName)) {
            return rs.next();
        }
    }
}
