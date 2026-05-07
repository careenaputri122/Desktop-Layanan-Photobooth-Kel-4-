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

    public static final int MEMBER_COMPLETED_ORDER_TARGET = 3;

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
                if (booking.getUser() != null) {
                    UserDAO.getInstance().refreshMemberStatusById(booking.getUser().getId());
                }
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
        syncPastApprovedBookings();
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
        syncPastApprovedBookings();
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
        syncPastApprovedBookings();
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
        syncPastApprovedBookings();
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
        int userId = findUserIdByBookingId(id);
        String statusFinal = resolveFinalStatus(id, status);
        String sql = "UPDATE bookings SET status = ? WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, statusFinal);
            ps.setInt   (2, id);
            boolean updated = ps.executeUpdate() > 0;
            if (updated && userId > 0) {
                UserDAO.getInstance().refreshMemberStatusById(userId);
            }
            return updated;

        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public int countCompletedBookingsByUser(int userId) {
        syncPastApprovedBookings();
        String sql = "SELECT COUNT(*) FROM bookings WHERE user_id = ? AND status = 'Selesai'";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    public boolean isMemberEligible(int userId) {
        return countCompletedBookingsByUser(userId) >= MEMBER_COMPLETED_ORDER_TARGET;
    }

    public int countMemberEligibleUsers() {
        syncPastApprovedBookings();
        String sql = "SELECT COUNT(*) FROM (" +
                     "SELECT b.user_id FROM bookings b " +
                     "JOIN users u ON u.id = b.user_id " +
                     "WHERE b.status = 'Selesai' AND u.role <> 'admin' " +
                     "GROUP BY b.user_id " +
                     "HAVING COUNT(*) >= ?" +
                     ") member_users";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, MEMBER_COMPLETED_ORDER_TARGET);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    // ── Kalender Booking ──────────────────────────────────────────────────

    /** Batas maksimum booking per hari (3 sesi: pagi, siang, sore). */
    public static final int MAX_BOOKING_PER_DAY = 3;

    /**
     * Hitung jumlah booking pada tanggal EVENT tertentu
     * (hanya status yang aktif: Menunggu Konfirmasi / Disetujui / Selesai).
     *
     * @param tanggal tanggal acara (java.time.LocalDate)
     * @return jumlah booking pada hari itu
     */
    public int countBookingsByDate(java.time.LocalDate tanggal) {
        syncPastApprovedBookings();
        String sql = "SELECT COUNT(*) FROM bookings " +
                     "WHERE tanggal = ? AND status NOT IN ('Ditolak')";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(tanggal));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * Cek apakah tanggal sudah penuh (jumlah booking >= MAX_BOOKING_PER_DAY).
     *
     * @param tanggal tanggal yang dicek
     * @return true jika penuh / tidak bisa dipesan lagi
     */
    public boolean isDateFullyBooked(java.time.LocalDate tanggal) {
        return countBookingsByDate(tanggal) >= MAX_BOOKING_PER_DAY;
    }

    public List<Booking> findActiveByDate(java.time.LocalDate tanggal) {
        syncPastApprovedBookings();
        List<Booking> list = new ArrayList<>();
        String sql = "SELECT * FROM bookings " +
                     "WHERE tanggal = ? AND status NOT IN ('Ditolak') " +
                     "ORDER BY jam_mulai ASC";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, java.sql.Date.valueOf(tanggal));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    /**
     * Ambil semua tanggal yang sudah PENUH dalam rentang bulan tertentu.
     * Digunakan oleh kalender untuk mewarnai tanggal merah.
     *
     * @param yearMonth bulan yang ditampilkan
     * @return Set tanggal yang sudah penuh
     */
    public java.util.Set<java.time.LocalDate> getFullyBookedDatesInMonth(java.time.YearMonth yearMonth) {
        syncPastApprovedBookings();
        java.util.Set<java.time.LocalDate> fullDates = new java.util.HashSet<>();
        String sql = "SELECT tanggal, COUNT(*) AS total FROM bookings " +
                     "WHERE tanggal BETWEEN ? AND ? AND status NOT IN ('Ditolak') " +
                     "GROUP BY tanggal HAVING total >= ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(yearMonth.atDay(1)));
            ps.setDate(2, java.sql.Date.valueOf(yearMonth.atEndOfMonth()));
            ps.setInt (3, MAX_BOOKING_PER_DAY);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                fullDates.add(rs.getDate("tanggal").toLocalDate());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return fullDates;
    }

    /**
     * Hitung jumlah pesanan yang DIBUAT hari ini (bukan tanggal eventnya).
     * Menggunakan kolom created_at dengan CURDATE() di MySQL.
     */
    public int countTodayOrders() {
        syncPastApprovedBookings();
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

    private void syncPastApprovedBookings() {
        String sql = "UPDATE bookings SET status = 'Selesai' " +
                     "WHERE status = 'Disetujui' AND tanggal < CURDATE()";
        try (Connection conn = getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private String resolveFinalStatus(int bookingId, String status) {
        if (!"Disetujui".equalsIgnoreCase(status)) return status;

        String sql = "SELECT tanggal FROM bookings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                java.sql.Date tanggal = rs.getDate("tanggal");
                if (tanggal != null && tanggal.toLocalDate().isBefore(java.time.LocalDate.now())) {
                    return "Selesai";
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return status;
    }

    private int findUserIdByBookingId(int bookingId) {
        String sql = "SELECT user_id FROM bookings WHERE id = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, bookingId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt("user_id");

        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

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
