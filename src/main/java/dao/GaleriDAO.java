package dao;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * GaleriDAO — mengelola data galeri foto admin ke database.
 * Kolom tabel `galeri`: id, judul, tema, tanggal_event, jumlah_foto, link_album, file_path, created_at
 */
public class GaleriDAO extends BaseDao {

    private static GaleriDAO instance;

    private GaleriDAO() {}

    public static GaleriDAO getInstance() {
        if (instance == null) instance = new GaleriDAO();
        return instance;
    }

    /** Simpan entri galeri baru */
    public boolean save(String judul, String tema, String tanggalEvent,
                        int jumlahFoto, String linkAlbum, String filePath) {
        String sql = "INSERT INTO galeri (judul, tema, tanggal_event, jumlah_foto, link_album, file_path) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, judul);
            ps.setString(2, tema);
            ps.setString(3, tanggalEvent);
            ps.setInt   (4, jumlahFoto);
            ps.setString(5, linkAlbum);
            ps.setString(6, filePath);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Hapus entri galeri berdasarkan ID */
    public boolean delete(int id) {
        String sql = "DELETE FROM galeri WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /** Ambil semua entri galeri, urut terbaru */
    public List<String[]> findAll() {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, judul, tema, tanggal_event, jumlah_foto, link_album, file_path " +
                     "FROM galeri ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("judul"),
                    rs.getString("tema"),
                    rs.getString("tanggal_event"),
                    String.valueOf(rs.getInt("jumlah_foto")),
                    rs.getString("link_album") != null ? rs.getString("link_album") : "",
                    rs.getString("file_path")  != null ? rs.getString("file_path")  : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /** Ambil entri galeri berdasarkan tema (case-insensitive) */
    public List<String[]> findByTema(String tema) {
        if (tema == null || tema.equals("Semua")) return findAll();
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT id, judul, tema, tanggal_event, jumlah_foto, link_album, file_path " +
                     "FROM galeri WHERE tema = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tema);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    String.valueOf(rs.getInt("id")),
                    rs.getString("judul"),
                    rs.getString("tema"),
                    rs.getString("tanggal_event"),
                    String.valueOf(rs.getInt("jumlah_foto")),
                    rs.getString("link_album") != null ? rs.getString("link_album") : "",
                    rs.getString("file_path")  != null ? rs.getString("file_path")  : ""
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}
