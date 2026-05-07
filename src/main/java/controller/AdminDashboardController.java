package controller;

import dao.BookingDAO;
import dao.UserDAO;
import model.Booking;
import model.User;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Pos;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class AdminDashboardController {

    // ── Sidebar Profile ───────────────────────────────────────────────────
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;
    @FXML private Label labelAdminInitial;

    // ── Stat Cards ────────────────────────────────────────────────────────
    @FXML private Label labelPesananHariIni;
    @FXML private Label labelMemberAktif;
    @FXML private Label labelPesananPending;

    // ── Tabel Pesanan Terbaru ─────────────────────────────────────────────
    @FXML private TableView<Booking>           tableBooking;
    @FXML private TableColumn<Booking, String> colNomor;
    @FXML private TableColumn<Booking, String> colNamaPemesan;
    @FXML private TableColumn<Booking, String> colPaket;
    @FXML private TableColumn<Booking, String> colTanggal;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, String> colTotal;

    @FXML private Button btnLogout;

    private final NumberFormat     rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private final SimpleDateFormat dateFmt   = new SimpleDateFormat("dd MMM yyyy");

    @FXML
    public void initialize() {
        setupSidebarProfile();
        setupTable();
        refresh(); // FIX: gunakan satu method refresh() agar mudah dipanggil ulang
    }

    // ── FIX: Method refresh() — dipanggil initialize() dan setelah kembali
    //    dari halaman lain (misalnya via SceneManager.showAdminDashboard())
    public void refresh() {
        loadStats();
        loadRecentBookings();
    }

    private void setupSidebarProfile() {
        User admin = UserDAO.getInstance().getCurrentUser();
        if (admin != null) {
            labelAdminName.setText(admin.getNamaDepan() + " " + admin.getNamaBelakang());
            labelAdminRole.setText("Administrator");
            labelAdminInitial.setText(String.valueOf(admin.getNamaDepan().charAt(0)).toUpperCase());
        }
    }

    private void setupTable() {
        tableBooking.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colNomor.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNomorPesanan() != null ? data.getValue().getNomorPesanan() : "-"));

        colNamaPemesan.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNamaPemesan() != null ? data.getValue().getNamaPemesan() : "-"));

        colPaket.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPaket() != null ? data.getValue().getPaket().getNama() : "-"));

        colTanggal.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTanggal() != null ? dateFmt.format(data.getValue().getTanggal()) : "-"));

        colStatus.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus() != null ? data.getValue().getStatus() : "-"));

        colTotal.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                rupiahFmt.format(data.getValue().getTotalHarga())));

        // Badge warna status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setText(null); setStyle(""); return; }
                setText(status);
                switch (status.toLowerCase()) {
                    case "menunggu konfirmasi" -> setStyle("-fx-text-fill: #D97706; -fx-font-weight: bold;");
                    case "disetujui"           -> setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                    case "ditolak"             -> setStyle("-fx-text-fill: #DC2626; -fx-font-weight: bold;");
                    case "selesai"             -> setStyle("-fx-text-fill: #7C3AED; -fx-font-weight: bold;");
                    default                    -> setStyle("-fx-text-fill: #6B7280;");
                }
            }
        });
    }

    private void loadStats() {
        List<User> semuaUser = UserDAO.getInstance().findAll();

        int pesananHariIni = BookingDAO.getInstance().countTodayOrders();

        long memberAktif = semuaUser.stream()
            .filter(u -> !"admin".equalsIgnoreCase(u.getRole()))
            .count();

        // FIX: Hitung langsung dari DB, bukan cache — memastikan angka selalu fresh
        long pending = BookingDAO.getInstance().findByStatus("Menunggu Konfirmasi").size();

        labelPesananHariIni.setText(String.valueOf(pesananHariIni));
        labelMemberAktif.setText(String.valueOf(memberAktif));
        labelPesananPending.setText(String.valueOf(pending));
    }

    private void loadRecentBookings() {
        List<Booking> semua = BookingDAO.getInstance().findAll();
        List<Booking> lima  = semua.stream().limit(5).collect(Collectors.toList());
        tableBooking.setItems(FXCollections.observableArrayList(lima));
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

    @FXML private void goUploadGaleri() { System.out.println("TODO: Upload Galeri"); }
    @FXML private void goKalender()     { System.out.println("TODO: Kalender Booking"); }
    @FXML private void goPelanggan()    { System.out.println("TODO: Pelanggan"); }
}