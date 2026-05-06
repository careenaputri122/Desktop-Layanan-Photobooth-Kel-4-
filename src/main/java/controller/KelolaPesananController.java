package controller;

import dao.BookingDAO;
import dao.UserDAO;
import model.Booking;
import model.User;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class KelolaPesananController {

    // ── Sidebar Profile ───────────────────────────────────────────
    @FXML private Label labelAdminInitial;
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;

    // ── Filter Buttons ────────────────────────────────────────────
    @FXML private Button btnFilterSemua;
    @FXML private Button btnFilterMenunggu;
    @FXML private Button btnFilterDisetujui;
    @FXML private Button btnFilterDitolak;
    @FXML private Button btnFilterSelesai;

    // ── Label Count ───────────────────────────────────────────────
    @FXML private Label labelJumlahPesanan;

    // ── Tabel ─────────────────────────────────────────────────────
    @FXML private TableView<Booking>           tablePesanan;
    @FXML private TableColumn<Booking, String> colNomor;
    @FXML private TableColumn<Booking, String> colNama;
    @FXML private TableColumn<Booking, String> colPaket;
    @FXML private TableColumn<Booking, String> colTanggal;
    @FXML private TableColumn<Booking, String> colTotal;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, Void>   colAksi;

    // ── Format Helper ─────────────────────────────────────────────
    private final SimpleDateFormat dateFmt  = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
    private final NumberFormat     rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

    // ── State filter aktif ────────────────────────────────────────
    private String filterAktif = "semua";

    // ─────────────────────────────────────────────────────────────

    @FXML
    public void initialize() {
        setupSidebarProfile();
        setupTable();
        loadData("semua");
    }

    // ── Setup Profile Sidebar ─────────────────────────────────────

    private void setupSidebarProfile() {
        User admin = UserDAO.getInstance().getCurrentUser();
        if (admin != null) {
            labelAdminName.setText(admin.getNamaDepan() + " " + admin.getNamaBelakang());
            labelAdminRole.setText("Administrator");
            labelAdminInitial.setText(
                String.valueOf(admin.getNamaDepan().charAt(0)).toUpperCase()
            );
        }
    }

    // ── Setup Kolom Tabel ─────────────────────────────────────────

    private void setupTable() {
        tablePesanan.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        // Nomor Pesanan
        colNomor.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNomorPesanan() != null
                    ? data.getValue().getNomorPesanan() : "-"
            )
        );
        colNomor.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); }
                else {
                    setText(val);
                    setStyle("-fx-text-fill: #EC4899; -fx-font-weight: bold; -fx-font-size: 12px;");
                }
            }
        });

        // Nama Pemesan
        colNama.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNamaPemesan() != null
                    ? data.getValue().getNamaPemesan() : "-"
            )
        );

        // Paket
        colPaket.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPaket() != null
                    ? data.getValue().getPaket().getNama() : "-"
            )
        );

        // Tanggal Event
        colTanggal.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTanggal() != null
                    ? dateFmt.format(data.getValue().getTanggal()) : "-"
            )
        );

        // Total Harga
        colTotal.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                rupiahFmt.format(data.getValue().getTotalHarga())
            )
        );

        // Status — badge warna
        colStatus.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus() != null
                    ? data.getValue().getStatus() : "-"
            )
        );
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER_LEFT);
            }
            @Override
            protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }

                badge.setText(status);
                badge.getStyleClass().setAll(resolveBadgeClass(status));
                setGraphic(badge);
            }
        });

        // Aksi — tombol ✓ dan ✗
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnSetujui = new Button("✓");
            private final Button btnTolak   = new Button("✗");
            private final HBox   box        = new HBox(6, btnSetujui, btnTolak);

            {
                box.setAlignment(Pos.CENTER);
                btnSetujui.getStyleClass().add("btn-setujui");
                btnTolak.getStyleClass().add("btn-tolak");

                btnSetujui.setOnAction(e -> handleAksi(getIndex(), "Disetujui"));
                btnTolak.setOnAction(e   -> handleAksi(getIndex(), "Ditolak"));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                Booking b = getTableView().getItems().get(getIndex());
                String  s = b.getStatus() != null ? b.getStatus() : "";

                // Tombol hanya aktif saat masih "Menunggu Konfirmasi"
                boolean bisaDiubah = s.equalsIgnoreCase("Menunggu Konfirmasi");
                btnSetujui.setDisable(!bisaDiubah);
                btnTolak.setDisable(!bisaDiubah);

                // Ganti style supaya terlihat disabled secara visual
                if (bisaDiubah) {
                    btnSetujui.getStyleClass().setAll("btn-setujui");
                    btnTolak.getStyleClass().setAll("btn-tolak");
                } else {
                    btnSetujui.getStyleClass().setAll("btn-aksi-disabled");
                    btnTolak.getStyleClass().setAll("btn-aksi-disabled");
                }

                setGraphic(box);
            }
        });

        // Klik baris → tampilkan popup detail
        tablePesanan.setRowFactory(tv -> {
            TableRow<Booking> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                // Klik single dan baris tidak kosong
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    // Pastikan klik bukan di tombol aksi
                    if (!(event.getTarget() instanceof Button)) {
                        showDetailPopup(row.getItem());
                    }
                }
            });
            return row;
        });
    }

    // ── Load Data ─────────────────────────────────────────────────

    private void loadData(String filter) {
        this.filterAktif = filter;
        List<Booking> data;

        switch (filter) {
            case "menunggu"  -> data = BookingDAO.getInstance().findByStatus("Menunggu Konfirmasi");
            case "disetujui" -> data = BookingDAO.getInstance().findByStatus("Disetujui");
            case "ditolak"   -> data = BookingDAO.getInstance().findByStatus("Ditolak");
            case "selesai"   -> data = BookingDAO.getInstance().findByStatus("Selesai");
            default          -> data = BookingDAO.getInstance().findAll();
        }

        ObservableList<Booking> items = FXCollections.observableArrayList(data);
        tablePesanan.setItems(items);

        // Update label count
        labelJumlahPesanan.setText(data.size() + " pesanan");

        // Update visual tombol filter aktif
        updateFilterStyle(filter);
    }

    // ── Aksi ✓ / ✗ ────────────────────────────────────────────────

    private void handleAksi(int index, String statusBaru) {
        Booking booking = tablePesanan.getItems().get(index);
        if (booking == null) return;

        String konfirmasi = statusBaru.equals("Disetujui")
            ? "Setujui pesanan " + booking.getNomorPesanan() + "?"
            : "Tolak pesanan "   + booking.getNomorPesanan() + "?";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, konfirmasi, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Konfirmasi Aksi");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                // updateStatus di DAO sudah handle auto-Selesai jika tanggal lewat
                boolean ok = BookingDAO.getInstance().updateStatus(booking.getId(), statusBaru);
                if (ok) {
                    loadData(filterAktif); // refresh tabel dengan filter yang sama
                } else {
                    showError("Gagal mengubah status pesanan. Coba lagi.");
                }
            }
        });
    }

    // ── Popup Detail ──────────────────────────────────────────────

    private void showDetailPopup(Booking b) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Detail Pesanan");
        popup.setResizable(false);

        // Header gradient pink
        VBox header = new VBox(4);
        header.getStyleClass().add("popup-header");

        Label lblJudul = new Label("📋  Detail Pesanan");
        lblJudul.getStyleClass().add("popup-title");

        Label lblNomor = new Label(
            b.getNomorPesanan() != null ? b.getNomorPesanan() : "-"
        );
        lblNomor.getStyleClass().add("popup-nomor");

        header.getChildren().addAll(lblJudul, lblNomor);

        // Body
        VBox body = new VBox(10);
        body.getStyleClass().add("popup-body");

        // ── Bagian Informasi Pesanan
        body.getChildren().add(sectionTitle("INFORMASI PESANAN"));
        body.getChildren().add(fieldRow("Paket",
            b.getPaket() != null ? b.getPaket().getNama() : "-"));
        body.getChildren().add(fieldRow("Tanggal Event",
            b.getTanggal() != null ? dateFmt.format(b.getTanggal()) : "-"));
        body.getChildren().add(fieldRow("Jam Mulai",
            b.getJamMulai() != null ? b.getJamMulai() : "-"));
        body.getChildren().add(fieldRow("Lokasi",
            b.getLokasi() != null ? b.getLokasi() : "-"));
        body.getChildren().add(fieldRow("Total Harga",
            rupiahFmt.format(b.getTotalHarga())));
        body.getChildren().add(fieldRow("Status",
            b.getStatus() != null ? b.getStatus() : "-"));

        body.getChildren().add(popupSeparator());

        // ── Bagian Data Pemesan
        body.getChildren().add(sectionTitle("DATA PEMESAN"));
        body.getChildren().add(fieldRow("Nama",
            b.getNamaPemesan() != null ? b.getNamaPemesan() : "-"));
        body.getChildren().add(fieldRow("Email",
            b.getEmail() != null ? b.getEmail() : "-"));
        body.getChildren().add(fieldRow("No. WhatsApp",
            b.getPhone() != null ? b.getPhone() : "-"));

        // Catatan (hanya tampil jika ada)
        String catatan = b.getCatatan();
        if (catatan != null && !catatan.isBlank()) {
            body.getChildren().add(popupSeparator());
            body.getChildren().add(sectionTitle("CATATAN"));
            Label lblCatatan = new Label(catatan);
            lblCatatan.setWrapText(true);
            lblCatatan.setStyle(
                "-fx-text-fill: #374151; -fx-font-size: 13px; " +
                "-fx-background-color: #F9FAFB; -fx-padding: 8 12; " +
                "-fx-background-radius: 8;"
            );
            lblCatatan.setMaxWidth(360);
            body.getChildren().add(lblCatatan);
        }

        body.getChildren().add(popupSeparator());

        // Tombol tutup
        Button btnTutup = new Button("Tutup");
        btnTutup.getStyleClass().add("popup-btn-close");
        btnTutup.setMaxWidth(Double.MAX_VALUE);
        btnTutup.setOnAction(e -> popup.close());
        body.getChildren().add(btnTutup);

        // Rakit layout popup
        VBox root = new VBox();
        root.getStyleClass().add("popup-detail-root");
        root.getChildren().addAll(header, body);

        Scene scene = new Scene(root, 420, 0); // tinggi auto
        scene.getStylesheets().add(
            getClass().getResource("/view/kelola_pesanan.css").toExternalForm()
        );

        popup.setScene(scene);
        popup.sizeToScene();
        popup.centerOnScreen();
        popup.showAndWait();
    }

    // ── Helper Popup ──────────────────────────────────────────────

    private Label sectionTitle(String text) {
        Label lbl = new Label(text);
        lbl.getStyleClass().add("popup-section-title");
        VBox.setMargin(lbl, new Insets(4, 0, 0, 0));
        return lbl;
    }

    private HBox fieldRow(String label, String value) {
        Label lblKey = new Label(label);
        lblKey.getStyleClass().add("popup-field-label");

        Label lblVal = new Label(value);
        lblVal.getStyleClass().add("popup-field-value");
        lblVal.setWrapText(true);
        lblVal.setMaxWidth(220);

        HBox row = new HBox(12, lblKey, lblVal);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private Separator popupSeparator() {
        Separator sep = new Separator();
        sep.getStyleClass().add("popup-separator");
        VBox.setMargin(sep, new Insets(4, 0, 4, 0));
        return sep;
    }

    // ── Helper Badge ──────────────────────────────────────────────

    private String resolveBadgeClass(String status) {
        if (status == null) return "badge-menunggu";
        return switch (status.toLowerCase()) {
            case "disetujui" -> "badge-disetujui";
            case "ditolak"   -> "badge-ditolak";
            case "selesai"   -> "badge-selesai";
            default          -> "badge-menunggu"; // menunggu konfirmasi
        };
    }

    // ── Update Visual Filter ──────────────────────────────────────

    private void updateFilterStyle(String aktif) {
        // Reset semua ke default
        for (Button btn : new Button[]{
            btnFilterSemua, btnFilterMenunggu,
            btnFilterDisetujui, btnFilterDitolak, btnFilterSelesai
        }) {
            btn.getStyleClass().setAll("filter-btn");
        }

        // Set yang aktif
        Button target = switch (aktif) {
            case "menunggu"  -> btnFilterMenunggu;
            case "disetujui" -> btnFilterDisetujui;
            case "ditolak"   -> btnFilterDitolak;
            case "selesai"   -> btnFilterSelesai;
            default          -> btnFilterSemua;
        };
        target.getStyleClass().setAll("filter-btn-active");
    }

    // ── Handler Filter Tab ────────────────────────────────────────

    @FXML private void filterSemua()     { loadData("semua");     }
    @FXML private void filterMenunggu()  { loadData("menunggu");  }
    @FXML private void filterDisetujui() { loadData("disetujui"); }
    @FXML private void filterDitolak()   { loadData("ditolak");   }
    @FXML private void filterSelesai()   { loadData("selesai");   }

    // ── Navigasi Sidebar ──────────────────────────────────────────

    @FXML private void handleLogout() {
        UserDAO.getInstance().logout();
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goDashboard()      {
        try { SceneManager.showAdminDashboard(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goKelolaPesanan()  { /* halaman ini sendiri, tidak perlu navigate */ }
    @FXML private void goKelolaPaket()    {
        try { SceneManager.showKelolaPaket(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goUploadGaleri()   { System.out.println("TODO: Upload Galeri");  }
    @FXML private void goKalender()       { System.out.println("TODO: Kalender");       }
    @FXML private void goPelanggan()      { System.out.println("TODO: Pelanggan");      }

    // ── Util ──────────────────────────────────────────────────────

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}
