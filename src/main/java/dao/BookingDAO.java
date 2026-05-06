package dao;

import model.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * BookingDAO — mengelola data pemesanan ke database.
 *
 * Konsep OOP yang diterapkan:
 *  - INHERITANCE  : extends BaseDao → tidak perlu hardcode koneksi DB lagi
 *  - POLYMORPHISM : implements IDao<Booking> → bisa diperlakukan seragam
 *  - SINGLETON    : satu instance shared untuk seluruh aplikasi
 *
 * Kolom tabel `bookings`:
 *   id, user_id, paket_id, tanggal, jam_mulai, lokasi,
 *   nama_pemesan, email, phone, catatan, status, nomor_pesanan, total_harga, created_at
 */
public class BookingDAO extends BaseDao implements IDao<Booking> {

    // ── Singleton ─────────────────────────────────────────────────────────
    private static BookingDAO instance;

    private BookingDAO() {}

    public static BookingDAO getInstance() {
        if (instance == null) instance = new BookingDAO();
        return instance;
    }

    // ── Implementasi IDao ─────────────────────────────────────────────────

    /**
     * Simpan booking baru ke database.
     *
     * @param booking objek Booking yang akan disimpan
     * @return true jika berhasil tersimpan
     */
    @Override
    public boolean save(Booking booking) {
        String sql = "INSERT INTO bookings " +
                     "(user_id, paket_id, tanggal, jam_mulai, lokasi, " +
                     "nama_pemesan, email, phone, catatan, status, nomor_pesanan, total_harga) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt   (1,  booking.getUser()  != null ? booking.getUser().getId()  : 0);
            ps.setInt   (2,  booking.getPaket() != null ? booking.getPaket().getId() : 0);
            ps.setDate  (3,  new java.sql.Date(booking.getTanggal().getTime()));
            ps.setString(4,  booking.getJamMulai());
            ps.setString(5,  booking.getLokasi());
            ps.setString(6,  booking.getNamaPemesan());
            ps.setString(7,  booking.getEmail());
            ps.setString(8,  booking.getPhone());
            ps.setString(9,  booking.getCatatan());
            ps.setString(10, booking.getStatus());
            ps.setString(11, booking.getNomorPesanan());
            ps.setDouble(12, booking.getTotalHarga());

            int rows = ps.executeUpdate();
            if (rows > 0) {
                ResultSet keys = ps.getGeneratedKeys();
                if (keys.next()) booking.setId(keys.getInt(1));
                return true;
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    /** Update hanya kolom status (paling umum dipakai). */
    @Override
    public boolean update(Booking booking) {
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, booking.getStatus());
            ps.setInt   (2, booking.getId());
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean delete(int id) {
        String sql = "DELETE FROM bookings WHERE id = ?";
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
    public Booking findById(int id) {
        String sql = "SELECT * FROM bookings WHERE id = ?";
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
    public List<Booking> findAll() {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings ORDER BY id DESC";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Ambil semua booking milik user tertentu.
     * Berguna untuk halaman "Riwayat Pesanan" sisi member.
     */
    public List<Booking> findByUser(int userId) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE user_id = ? ORDER BY tanggal DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * ✅ BARU — Ambil pesanan berdasarkan status.
     * Dipakai oleh filter tab di halaman Kelola Pesanan admin.
     *
     * @param status nilai status, contoh: "Menunggu Konfirmasi", "Disetujui", "Ditolak", "Selesai"
     * @return list booking yang cocok
     */
    public List<Booking> findByStatus(String status) {
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings WHERE status = ? ORDER BY id DESC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * ✅ BARU — Update hanya kolom status berdasarkan ID booking.
     * Dipakai oleh tombol ✓ (Disetujui) dan ✗ (Ditolak) di halaman Kelola Pesanan.
     *
     * Aturan otomatis:
     *  - Jika status baru = "Disetujui" DAN tanggal event sudah lewat → disimpan sebagai "Selesai"
     *
     * @param id     ID booking yang akan diupdate
     * @param status status baru ("Disetujui" / "Ditolak")
     * @return true jika berhasil
     */
    public boolean updateStatus(int id, String status) {
        // Cek apakah perlu auto-set ke "Selesai"
        String statusFinal = status;
        if ("Disetujui".equals(status)) {
            Booking b = findById(id);
            if (b != null && b.getTanggal() != null) {
                // Tanggal event sudah lewat → langsung Selesai
                java.time.LocalDate tanggalEvent = b.getTanggal()
                        .toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();
                if (tanggalEvent.isBefore(java.time.LocalDate.now())) {
                    statusFinal = "Selesai";
                }
            }
        }

        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statusFinal);
            ps.setInt   (2, id);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Hitung jumlah pesanan yang DIBUAT hari ini (bukan tanggal eventnya).
     * Menggunakan kolom created_at dengan CURDATE() di MySQL.
     */
    public int countTodayOrders() {
        String sql = "SELECT COUNT(*) FROM bookings WHERE DATE(created_at) = CURDATE()";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {

            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Generate nomor pesanan unik.
     * Format: FTM-YYYY-XXX (contoh: FTM-2026-042)
     */
    public static String generateNomorPesanan() {
        int tahun = java.time.LocalDate.now().getYear();
        int acak  = (int)(Math.random() * 900) + 100;
        return "FTM-" + tahun + "-" + acak;
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private Booking mapRow(ResultSet rs) throws SQLException {
        Booking b = new Booking();
        b.setId          (rs.getInt   ("id"));
        b.setJamMulai    (rs.getString("jam_mulai"));
        b.setLokasi      (rs.getString("lokasi"));
        b.setNamaPemesan (rs.getString("nama_pemesan"));
        b.setEmail       (rs.getString("email"));
        b.setPhone       (rs.getString("phone"));
        b.setCatatan     (rs.getString("catatan"));
        b.setStatus      (rs.getString("status"));
        b.setNomorPesanan(rs.getString("nomor_pesanan"));
        b.setTotalHarga  (rs.getDouble("total_harga"));
        b.setTanggal     (rs.getDate  ("tanggal"));

        // Lazy load user & paket hanya dengan ID-nya
        int userId  = rs.getInt("user_id");
        int paketId = rs.getInt("paket_id");
        if (userId  > 0) b.setUser (UserDAO.getInstance().findById(userId));
        if (paketId > 0) b.setPaket(PaketDAO.getInstance().findById(paketId));

        return b;
    }
}
