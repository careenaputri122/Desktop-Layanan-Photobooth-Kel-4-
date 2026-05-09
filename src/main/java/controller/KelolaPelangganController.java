package controller;

import dao.BookingDAO;
import dao.UserDAO;
import model.User;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.List;
import java.util.stream.Collectors;

/**
 * KelolaPelangganController — Menampilkan daftar pelanggan yang pernah
 * memiliki pesanan selesai. Pelanggan dengan >= 3 pesanan selesai
 * otomatis ditandai sebagai Member.
 */
public class KelolaPelangganController {

    // ── Sidebar Profile ───────────────────────────────────────────────────
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;
    @FXML private Label labelAdminInitial;

    // ── Stat Cards ────────────────────────────────────────────────────────
    @FXML private Label labelTotalPelanggan;
    @FXML private Label labelTotalMember;
    @FXML private Label labelTotalRegular;

    // ── Filter ────────────────────────────────────────────────────────────
    @FXML private TextField fieldCari;
    @FXML private ComboBox<String> comboFilter;

    // ── Tabel Pelanggan ───────────────────────────────────────────────────
    @FXML private TableView<PelangganRow>              tablePelanggan;
    @FXML private TableColumn<PelangganRow, String>    colNama;
    @FXML private TableColumn<PelangganRow, String>    colEmail;
    @FXML private TableColumn<PelangganRow, Integer>   colJumlahOrder;
    @FXML private TableColumn<PelangganRow, String>    colStatus;

    private ObservableList<PelangganRow> allData = FXCollections.observableArrayList();

    // ── Inner class data row ──────────────────────────────────────────────
    public static class PelangganRow {
        private final User   user;
        private final int    jumlahSelesai;
        private final boolean isMember;

        public PelangganRow(User user, int jumlahSelesai) {
            this.user          = user;
            this.jumlahSelesai = jumlahSelesai;
            this.isMember      = jumlahSelesai >= BookingDAO.MEMBER_COMPLETED_ORDER_TARGET;
        }

        public User    getUser()          { return user; }
        public int     getJumlahSelesai() { return jumlahSelesai; }
        public boolean isMember()         { return isMember; }
        public String  getNamaLengkap()   { return user.getNamaDepan() + " " + user.getNamaBelakang(); }
        public String  getEmail()         { return user.getEmail(); }
        public String  getStatusLabel()   { return isMember ? "Member ⭐" : "Regular"; }
    }

    // ── Initialize ────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupSidebarProfile();
        setupFilter();
        setupTable();
        loadData();
    }

    private void setupSidebarProfile() {
        User admin = UserDAO.getInstance().getCurrentUser();
        if (admin != null) {
            labelAdminName.setText(admin.getNamaDepan() + " " + admin.getNamaBelakang());
            labelAdminRole.setText("Administrator");
            labelAdminInitial.setText(String.valueOf(admin.getNamaDepan().charAt(0)).toUpperCase());
        }
    }

    private void setupFilter() {
        comboFilter.setItems(FXCollections.observableArrayList("Semua", "Member", "Regular"));
        comboFilter.setValue("Semua");
        comboFilter.setOnAction(e -> applyFilter());
        fieldCari.textProperty().addListener((obs, old, val) -> applyFilter());
    }

    private void setupTable() {
        tablePelanggan.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colNama.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNamaLengkap()));

        colEmail.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEmail()));

        colJumlahOrder.setCellValueFactory(data ->
            new SimpleIntegerProperty(data.getValue().getJumlahSelesai()).asObject());

        colStatus.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getStatusLabel()));

        // Badge warna status
        colStatus.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(status);
                if (status.startsWith("Member")) {
                    setStyle("-fx-text-fill: #7C3AED; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #059669; -fx-font-weight: bold;");
                }
            }
        });

        // Badge warna jumlah order
        colJumlahOrder.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                    return;
                }
                setText(val.toString());
                if (val >= BookingDAO.MEMBER_COMPLETED_ORDER_TARGET) {
                    setStyle("-fx-text-fill: #7C3AED; -fx-font-weight: bold;");
                } else {
                    setStyle("-fx-text-fill: #374151;");
                }
            }
        });
    }

    private void loadData() {
        allData.clear();
        List<User> semuaUser = UserDAO.getInstance().findAll();

        for (User u : semuaUser) {
            if ("admin".equalsIgnoreCase(u.getRole())) continue;
            // Sinkronkan status member di DB berdasarkan jumlah pesanan selesai
            UserDAO.getInstance().refreshMemberStatus(u);
            int selesai = BookingDAO.getInstance().countCompletedBookingsByUser(u.getId());
            if (selesai > 0) {
                allData.add(new PelangganRow(u, selesai));
            }
        }

        // Urutkan: member dulu, lalu berdasarkan jumlah order terbanyak
        allData.sort((a, b) -> {
            if (a.isMember() != b.isMember()) return b.isMember() ? 1 : -1;
            return Integer.compare(b.getJumlahSelesai(), a.getJumlahSelesai());
        });

        updateStats();
        applyFilter();
    }

    private void updateStats() {
        long totalMember  = allData.stream().filter(PelangganRow::isMember).count();
        long totalRegular = allData.stream().filter(r -> !r.isMember()).count();

        labelTotalPelanggan.setText(String.valueOf(allData.size()));
        labelTotalMember.setText(String.valueOf(totalMember));
        labelTotalRegular.setText(String.valueOf(totalRegular));
    }

    private void applyFilter() {
        String keyword = fieldCari.getText().trim().toLowerCase();
        String filter  = comboFilter.getValue();

        List<PelangganRow> filtered = allData.stream().filter(row -> {
            boolean matchKeyword = keyword.isEmpty()
                || row.getNamaLengkap().toLowerCase().contains(keyword)
                || row.getEmail().toLowerCase().contains(keyword);

            boolean matchFilter = "Semua".equals(filter)
                || ("Member".equals(filter)  && row.isMember())
                || ("Regular".equals(filter) && !row.isMember());

            return matchKeyword && matchFilter;
        }).collect(Collectors.toList());

        tablePelanggan.setItems(FXCollections.observableArrayList(filtered));
    }

    // ── Navigasi Sidebar ──────────────────────────────────────────────────

    @FXML private void handleLogout() {
        UserDAO.getInstance().logout();
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goDashboard() {
        try { SceneManager.showAdminDashboard(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goKelolaPesanan() {
        try { SceneManager.showKelolaPesanan(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goKelolaPaket() {
        try { SceneManager.showKelolaPaket(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goUploadGaleri() { System.out.println("TODO: Upload Galeri"); }

    @FXML private void goKalender() {
        try { SceneManager.showKalenderBooking(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void handleRefresh() {
        fieldCari.clear();
        comboFilter.setValue("Semua");
        loadData();
    }
}
