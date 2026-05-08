package dao;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.HashSet;
import java.util.Set;

/**
 * BlockedDateDAO — mengelola tanggal yang diblokir admin.
 */
public class BlockedDateDAO extends BaseDao {

    private static BlockedDateDAO instance;

    private BlockedDateDAO() {}

    public static BlockedDateDAO getInstance() {
        if (instance == null) instance = new BlockedDateDAO();
        return instance;
    }

    public boolean blockDate(LocalDate tanggal, String alasan, int createdBy) {
        String sql = "INSERT INTO blocked_dates (tanggal, alasan, created_by) " +
                     "VALUES (?, ?, ?) " +
                     "ON DUPLICATE KEY UPDATE alasan = VALUES(alasan), created_by = VALUES(created_by)";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tanggal));
            ps.setString(2, alasan);
            if (createdBy > 0) ps.setInt(3, createdBy); else ps.setNull(3, Types.INTEGER);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean unblockDate(LocalDate tanggal) {
        String sql = "DELETE FROM blocked_dates WHERE tanggal = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tanggal));
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public boolean isBlocked(LocalDate tanggal) {
        String sql = "SELECT COUNT(*) FROM blocked_dates WHERE tanggal = ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(tanggal));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    public Set<LocalDate> getBlockedDatesInMonth(YearMonth ym) {
        Set<LocalDate> dates = new HashSet<>();
        String sql = "SELECT tanggal FROM blocked_dates WHERE tanggal BETWEEN ? AND ?";
        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(ym.atDay(1)));
            ps.setDate(2, Date.valueOf(ym.atEndOfMonth()));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) dates.add(rs.getDate("tanggal").toLocalDate());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return dates;
    }
}
