package controller;

import dao.UserDAO;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import java.io.File;

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

    @FXML
    public void initialize() {
        setupSidebarProfile();
        setupKategori();
        setupTable();
        loadDefaultData();
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

    private void loadDefaultData() {
        daftarGaleri.addAll(
            new GaleriItem("Wedding Leddy & Cortis",             "Wedding",   "12 Apr 2026", "148 foto"),
            new GaleriItem("Birthday Dea Amalia ke-19",          "Birthday",  "20 Mar 2026", "95 foto"),
            new GaleriItem("Gathering PT. Dea Keren",            "Corporate", "05 Mar 2026", "210 foto"),
            new GaleriItem("Wisuda Universitas Sriwijaya",       "Wisuda",    "05 Mar 2026", "150 foto"),
            new GaleriItem("Birthday Aluna ke-18",               "Birthday",  "03 Mar 2026", "102 foto"),
            new GaleriItem("Wedding Tasya & Arga",               "Wedding",   "01 Mar 2026", "160 foto"),
            new GaleriItem("Wisuda Politeknik Negeri Sriwijaya", "Wisuda",    "26 Feb 2026", "135 foto"),
            new GaleriItem("Wedding Tiara & Fauzan",             "Wedding",   "22 Feb 2026", "175 foto")
        );
    }

    @FXML
    private void handlePilihFile() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Foto");
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
        String judul    = fieldJudul.getText().trim();
        String kategori = comboKategori.getValue();
        String tanggal  = fieldTanggal.getText().trim();
        String jumlah   = fieldJumlahFoto.getText().trim();

        if (judul.isEmpty() || tanggal.isEmpty() || jumlah.isEmpty()) {
            showStatus("⚠ Judul, tanggal, dan jumlah foto wajib diisi.", false);
            return;
        }

        daftarGaleri.add(0, new GaleriItem(judul, kategori, tanggal, jumlah + " foto"));
        clearForm();
        showStatus("✓ Galeri \"" + judul + "\" berhasil ditambahkan.", true);
    }

    @FXML
    private void handleHapus() {
        GaleriItem selected = tableGaleri.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showStatus("⚠ Pilih item galeri yang ingin dihapus.", false);
            return;
        }
        daftarGaleri.remove(selected);
        showStatus("✓ Galeri berhasil dihapus.", true);
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
    @FXML private void goUploadGaleri() {
        // sudah di halaman ini
    }
    @FXML private void goKalender() {
        try { SceneManager.showKalenderBooking(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goPelanggan() {
        try { SceneManager.showKelolaPelanggan(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goDashboard() {
        try { SceneManager.showAdminDashboard(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Inner class model ringan ──────────────────────────────────────────
    public static class GaleriItem {
        public final String judul, kategori, tanggal, jumlahFoto;
        public GaleriItem(String judul, String kategori, String tanggal, String jumlahFoto) {
            this.judul = judul; this.kategori = kategori;
            this.tanggal = tanggal; this.jumlahFoto = jumlahFoto;
        }
    }
}