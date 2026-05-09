package controller;

import dao.GaleriDAO;
import dao.UserDAO;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

public class UploadGaleriController {

    // ── Sidebar ───────────────────────────────────────────────────────────
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;
    @FXML private Label labelAdminInitial;

    // ── Form Upload ───────────────────────────────────────────────────────
    @FXML private TextField fieldJudul;
    @FXML private ComboBox<String> comboKategori;
    @FXML private TextField fieldTanggal;
    @FXML private TextField fieldJumlahFoto;
    @FXML private TextField fieldLink;
    @FXML private Label labelFilePath;
    @FXML private Label labelStatus;

    // ── Tabel Galeri ──────────────────────────────────────────────────────
    @FXML private TableView<GaleriItem> tableGaleri;
    @FXML private TableColumn<GaleriItem, String> colJudul;
    @FXML private TableColumn<GaleriItem, String> colKategori;
    @FXML private TableColumn<GaleriItem, String> colTanggal;
    @FXML private TableColumn<GaleriItem, String> colJumlah;

    private ObservableList<GaleriItem> daftarGaleri = FXCollections.observableArrayList();
    private String selectedFilePath = "";

    // Folder penyimpanan foto cover
    private static final String COVER_DIR = "cover_galeri";

    @FXML
    public void initialize() {
        setupSidebarProfile();
        setupKategori();
        setupTable();
        loadFromDatabase();
    }

    private void setupSidebarProfile() {
        User admin = UserDAO.getInstance().getCurrentUser();
        if (admin != null) {
            labelAdminName.setText(admin.getNamaDepan() + " " + admin.getNamaBelakang());
            labelAdminRole.setText("Administrator");
            labelAdminInitial.setText(String.valueOf(admin.getNamaDepan().charAt(0)).toUpperCase());
        }
    }

    private void setupKategori() {
        comboKategori.setItems(FXCollections.observableArrayList(
            "Wedding", "Birthday", "Corporate", "Wisuda", "Lainnya"
        ));
        comboKategori.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        tableGaleri.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colJudul.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().judul));
        colKategori.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().kategori));
        colTanggal.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().tanggal));
        colJumlah.setCellValueFactory(d ->
            new javafx.beans.property.SimpleStringProperty(d.getValue().jumlahFoto));

        tableGaleri.setItems(daftarGaleri);
    }

    // ── Load data dari database ────────────────────────────────────────────
    private void loadFromDatabase() {
        daftarGaleri.clear();
        try {
            List<String[]> dbData = GaleriDAO.getInstance().findAll();
            // dbData: [id, judul, tema, tanggal_event, jumlah_foto, link_album, file_path]
            for (String[] d : dbData) {
                daftarGaleri.add(new GaleriItem(
                    Integer.parseInt(d[0]),
                    d[1],
                    d[2],
                    d[3],
                    d[4] + " foto"
                ));
            }
        } catch (Exception e) {
            showStatus("⚠ Gagal memuat data dari database: " + e.getMessage(), false);
        }
    }

    @FXML
    private void handlePilihFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Foto Cover");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Gambar", "*.jpg", "*.jpeg", "*.png", "*.webp")
        );
        File file = fc.showOpenDialog(null);
        if (file != null) {
            selectedFilePath = file.getAbsolutePath();
            labelFilePath.setText(file.getName());
        }
    }

    @FXML
    private void handleSimpan() {
        String judul     = fieldJudul.getText().trim();
        String kategori  = comboKategori.getValue();
        String tanggal   = fieldTanggal.getText().trim();
        String jumlahStr = fieldJumlahFoto.getText().trim();
        String link      = fieldLink.getText().trim();

        if (judul.isEmpty() || tanggal.isEmpty() || jumlahStr.isEmpty()) {
            showStatus("⚠ Judul, tanggal, dan jumlah foto wajib diisi.", false);
            return;
        }

        int jumlah;
        try {
            jumlah = Integer.parseInt(jumlahStr);
        } catch (NumberFormatException e) {
            showStatus("⚠ Jumlah foto harus berupa angka.", false);
            return;
        }

        // Salin file foto ke folder penyimpanan permanen
        String savedFilePath = "";
        if (!selectedFilePath.isEmpty()) {
            savedFilePath = copyFotoToStorage(selectedFilePath);
        }

        // Simpan ke database
        boolean berhasil = GaleriDAO.getInstance().save(judul, kategori, tanggal, jumlah, link, savedFilePath);
        if (berhasil) {
            loadFromDatabase();
            clearForm();
            showStatus("✓ Galeri \"" + judul + "\" berhasil disimpan dan akan tampil di halaman galeri pelanggan.", true);
        } else {
            showStatus("⚠ Gagal menyimpan ke database. Periksa koneksi database.", false);
        }
    }

    @FXML
    private void handleHapus() {
        GaleriItem selected = tableGaleri.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("⚠ Pilih item galeri yang ingin dihapus.", false);
            return;
        }

        boolean berhasil = GaleriDAO.getInstance().delete(selected.id);
        if (berhasil) {
            daftarGaleri.remove(selected);
            showStatus("✓ Galeri \"" + selected.judul + "\" berhasil dihapus.", true);
        } else {
            showStatus("⚠ Gagal menghapus dari database. Periksa koneksi.", false);
        }
    }

    /**
     * Menyalin file foto ke folder permanen di home user agar bisa diakses lintas sesi.
     * Mengembalikan path absolut file yang telah disalin.
     */
    private String copyFotoToStorage(String sourcePath) {
        try {
            String appDir = System.getProperty("user.home") + File.separator
                          + "aksaf_photobooth" + File.separator + COVER_DIR;
            Path dirPath = Paths.get(appDir);
            if (!Files.exists(dirPath)) {
                Files.createDirectories(dirPath);
            }

            String sourceFile = Paths.get(sourcePath).getFileName().toString();
            String ext = sourceFile.contains(".")
                ? sourceFile.substring(sourceFile.lastIndexOf('.'))
                : ".jpg";
            String newFileName = System.currentTimeMillis() + ext;

            Path destPath = dirPath.resolve(newFileName);
            Files.copy(Paths.get(sourcePath), destPath, StandardCopyOption.REPLACE_EXISTING);
            return destPath.toAbsolutePath().toString();
        } catch (IOException e) {
            e.printStackTrace();
            return sourcePath;
        }
    }

    private void clearForm() {
        fieldJudul.clear();
        fieldTanggal.clear();
        fieldJumlahFoto.clear();
        fieldLink.clear();
        labelFilePath.setText("Belum ada file dipilih");
        selectedFilePath = "";
        comboKategori.getSelectionModel().selectFirst();
    }

    private void showStatus(String msg, boolean sukses) {
        labelStatus.setText(msg);
        labelStatus.setStyle(sukses
            ? "-fx-text-fill: #059669; -fx-font-weight: bold;"
            : "-fx-text-fill: #DC2626; -fx-font-weight: bold;");
    }

    // ── Navigasi Sidebar ──────────────────────────────────────────────────
    @FXML private void handleLogout() {
        UserDAO.getInstance().logout();
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goKelolaPesanan() {
        try { SceneManager.showKelolaPesanan(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goKelolaPaket() {
        try { SceneManager.showKelolaPaket(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goUploadGaleri() { /* sudah di halaman ini */ }
    @FXML private void goKalender() {
        try { SceneManager.showKalenderBooking(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goPelanggan() {
        try { SceneManager.showKelolaPelanggan(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goDashboard() {
        try { SceneManager.showAdminDashboard(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Inner class model ─────────────────────────────────────────────────
    public static class GaleriItem {
        public final int    id;
        public final String judul, kategori, tanggal, jumlahFoto;

        public GaleriItem(int id, String judul, String kategori, String tanggal, String jumlahFoto) {
            this.id        = id;
            this.judul     = judul;
            this.kategori  = kategori;
            this.tanggal   = tanggal;
            this.jumlahFoto = jumlahFoto;
        }
    }
}