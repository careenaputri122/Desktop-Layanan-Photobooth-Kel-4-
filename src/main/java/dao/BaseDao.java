package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * [TAMBAHAN OOP] Abstract class sebagai induk semua DAO.
 *
 * Konsep OOP yang diterapkan:
 *  - INHERITANCE: UserDAO, BookingDAO, PaketDAO semuanya extends kelas ini
 *  - ENCAPSULATION: konfigurasi DB disembunyikan di sini, tidak tersebar di tiap DAO
 *  - ABSTRACTION: kelas ini tidak bisa diinstansiasi langsung (abstract)
 */
public abstract class BaseDao {

    // ── Konfigurasi koneksi database (satu titik perubahan) ───────────────
    private static final String DB_URL  = "jdbc:mysql://localhost:3306/photobooth_db";
    private static final String DB_USER = "root";
    private static final String DB_PASS = "";

    /**
     * Mengembalikan koneksi baru ke database.
     * Dipanggil oleh subkelas saat ingin mengeksekusi query.
     *
     * @return Connection ke MySQL
     * @throws SQLException jika koneksi gagal
     */
    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASS);
    }
}