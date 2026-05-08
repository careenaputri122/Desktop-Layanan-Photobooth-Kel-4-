package dao;

import java.util.List;

/**
 * [TAMBAHAN OOP] Interface generik untuk semua DAO.
 *
 * Konsep OOP yang diterapkan:
 *  - ABSTRACTION: mendefinisikan kontrak tanpa implementasi
 *  - POLYMORPHISM: tiap DAO punya implementasi berbeda untuk metode yang sama
 *
 * Sebelumnya: tidak ada interface, setiap DAO berdiri sendiri tanpa kontrak.
 * Sekarang: semua DAO wajib mengimplementasikan operasi CRUD dasar ini.
 */
public interface IDao<T> {

    /** Simpan entitas baru ke database. */
    boolean save(T entity);

    /** Perbarui data entitas di database. */
    boolean update(T entity);

    /** Hapus entitas berdasarkan ID. */
    boolean delete(int id);

    /** Ambil entitas berdasarkan ID, atau null jika tidak ditemukan. */
    T findById(int id);

    /** Ambil semua entitas dari database. */
    List<T> findAll();
}