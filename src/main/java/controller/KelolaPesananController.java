package controller;

import dao.BookingDAO;
import dao.UserDAO;
import model.Booking;
import model.User;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
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

    @FXML private Label labelAdminInitial;
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;

    @FXML private Button btnFilterSemua;
    @FXML private Button btnFilterMenunggu;
    @FXML private Button btnFilterDisetujui;
    @FXML private Button btnFilterDitolak;
    @FXML private Button btnFilterSelesai;

    @FXML private Label labelJumlahPesanan;

    @FXML private TableView<Booking>           tablePesanan;
    @FXML private TableColumn<Booking, String> colNomor;
    @FXML private TableColumn<Booking, String> colNama;
    @FXML private TableColumn<Booking, String> colPaket;
    @FXML private TableColumn<Booking, String> colTanggal;
    @FXML private TableColumn<Booking, String> colTotal;
    @FXML private TableColumn<Booking, String> colStatus;
    @FXML private TableColumn<Booking, Void>   colAksi;

    private final SimpleDateFormat dateFmt   = new SimpleDateFormat("dd MMM yyyy", new Locale("id", "ID"));
    private final NumberFormat     rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private String filterAktif = "semua";

    @FXML
    public void initialize() {
        setupSidebarProfile();
        setupTable();
        loadData("semua");
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
        tablePesanan.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colNomor.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNomorPesanan() != null ? data.getValue().getNomorPesanan() : "-"));
        colNomor.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); }
                else { setText(val); setStyle("-fx-text-fill: #EC4899; -fx-font-weight: bold; -fx-font-size: 12px;"); }
            }
        });

        colNama.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getNamaPemesan() != null ? data.getValue().getNamaPemesan() : "-"));

        colPaket.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getPaket() != null ? data.getValue().getPaket().getNama() : "-"));

        colTanggal.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getTanggal() != null ? dateFmt.format(data.getValue().getTanggal()) : "-"));

        colTotal.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(rupiahFmt.format(data.getValue().getTotalHarga())));

        colStatus.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(
                data.getValue().getStatus() != null ? data.getValue().getStatus() : "-"));
        colStatus.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            { setContentDisplay(ContentDisplay.GRAPHIC_ONLY); setAlignment(Pos.CENTER_LEFT); }
            @Override protected void updateItem(String status, boolean empty) {
                super.updateItem(status, empty);
                if (empty || status == null) { setGraphic(null); return; }
                badge.setText(status);
                badge.getStyleClass().setAll(resolveBadgeClass(status));
                setGraphic(badge);
            }
        });

        // ─── FIX UTAMA: Simpan referensi Booking ke userData tombol ───────
        // Bug lama: getIndex() di lambda dikapturbaat tombol dibuat, bukan
        // saat diklik. Karena TableCell di-recycle JavaFX, index bisa salah.
        // Fix: setUserData(booking) tiap kali updateItem dipanggil,
        // lalu baca getUserData() saat tombol diklik.
        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnSetujui = new Button("✓");
            private final Button btnTolak   = new Button("✗");
            private final HBox   box        = new HBox(6, btnSetujui, btnTolak);
            {
                box.setAlignment(Pos.CENTER);
                btnSetujui.getStyleClass().add("btn-setujui");
                btnTolak.getStyleClass().add("btn-tolak");

                btnSetujui.setOnAction(e -> {
                    Booking b = (Booking) btnSetujui.getUserData();
                    if (b != null) handleAksi(b, "Disetujui");
                });
                btnTolak.setOnAction(e -> {
                    Booking b = (Booking) btnTolak.getUserData();
                    if (b != null) handleAksi(b, "Ditolak");
                });
            }

            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }

                Booking b = getTableView().getItems().get(getIndex());
                // Simpan referensi Booking terbaru ke tiap tombol
                btnSetujui.setUserData(b);
                btnTolak.setUserData(b);

                String  s = b.getStatus() != null ? b.getStatus() : "";
                boolean bisaDiubah = s.equalsIgnoreCase("Menunggu Konfirmasi");
                btnSetujui.setDisable(!bisaDiubah);
                btnTolak.setDisable(!bisaDiubah);

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

        // ─── FIX: Deteksi klik tombol yang benar ─────────────────────────
        // Bug lama: event.getTarget() instanceof Button tidak cukup karena
        // klik bisa mendarat di Label/Text di dalam Button, bukan Button-nya.
        // Fix: telusuri parent hierarchy sampai ketemu Button.
        tablePesanan.setRowFactory(tv -> {
            TableRow<Booking> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1) {
                    if (!isClickInsideButton(event.getTarget())) {
                        showDetailPopup(row.getItem());
                    }
                }
            });
            return row;
        });
    }

    /** Cek apakah node yang diklik berada di dalam Button (ancestor traversal). */
    private boolean isClickInsideButton(Object target) {
        if (!(target instanceof Node)) return false;
        Node node = (Node) target;
        while (node != null) {
            if (node instanceof Button) return true;
            node = node.getParent();
        }
        return false;
    }

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
        tablePesanan.setItems(FXCollections.observableArrayList(data));
        labelJumlahPesanan.setText(data.size() + " pesanan");
        updateFilterStyle(filter);
    }

    // ─── FIX: Terima Booking langsung, bukan index ──────────────────────
    private void handleAksi(Booking booking, String statusBaru) {
        String konfirmasi = statusBaru.equals("Disetujui")
            ? "Setujui pesanan " + booking.getNomorPesanan() + "?"
            : "Tolak pesanan "   + booking.getNomorPesanan() + "?";

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION, konfirmasi, ButtonType.YES, ButtonType.NO);
        alert.setHeaderText(null);
        alert.setTitle("Konfirmasi Aksi");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                boolean ok = BookingDAO.getInstance().updateStatus(booking.getId(), statusBaru);
                if (ok) {
                    loadData(filterAktif);
                } else {
                    showError("Gagal mengubah status pesanan. Coba lagi.");
                }
            }
        });
    }

    private void showDetailPopup(Booking b) {
        Stage popup = new Stage();
        popup.initModality(Modality.APPLICATION_MODAL);
        popup.setTitle("Detail Pesanan");
        popup.setResizable(false);

        VBox header = new VBox(4);
        header.getStyleClass().add("popup-header");
        Label lblJudul = new Label("📋  Detail Pesanan");
        lblJudul.getStyleClass().add("popup-title");
        Label lblNomor = new Label(b.getNomorPesanan() != null ? b.getNomorPesanan() : "-");
        lblNomor.getStyleClass().add("popup-nomor");
        header.getChildren().addAll(lblJudul, lblNomor);

        VBox body = new VBox(10);
        body.getStyleClass().add("popup-body");

        body.getChildren().add(sectionTitle("INFORMASI PESANAN"));
        body.getChildren().add(fieldRow("Paket",         b.getPaket()   != null ? b.getPaket().getNama() : "-"));
        body.getChildren().add(fieldRow("Tanggal Event", b.getTanggal() != null ? dateFmt.format(b.getTanggal()) : "-"));
        body.getChildren().add(fieldRow("Jam Mulai",     b.getJamMulai() != null ? b.getJamMulai() : "-"));
        body.getChildren().add(fieldRow("Lokasi",        b.getLokasi()   != null ? b.getLokasi()   : "-"));
        body.getChildren().add(fieldRow("Total Harga",   rupiahFmt.format(b.getTotalHarga())));
        body.getChildren().add(fieldRow("Status",        b.getStatus()   != null ? b.getStatus()   : "-"));
        body.getChildren().add(popupSeparator());

        body.getChildren().add(sectionTitle("DATA PEMESAN"));
        body.getChildren().add(fieldRow("Nama",         b.getNamaPemesan() != null ? b.getNamaPemesan() : "-"));
        body.getChildren().add(fieldRow("Email",        b.getEmail()       != null ? b.getEmail()       : "-"));
        body.getChildren().add(fieldRow("No. WhatsApp", b.getPhone()       != null ? b.getPhone()       : "-"));

        String catatan = b.getCatatan();
        if (catatan != null && !catatan.isBlank()) {
            body.getChildren().add(popupSeparator());
            body.getChildren().add(sectionTitle("CATATAN"));
            Label lblCatatan = new Label(catatan);
            lblCatatan.setWrapText(true);
            lblCatatan.setStyle("-fx-text-fill: #374151; -fx-font-size: 13px; " +
                "-fx-background-color: #F9FAFB; -fx-padding: 8 12; -fx-background-radius: 8;");
            lblCatatan.setMaxWidth(360);
            body.getChildren().add(lblCatatan);
        }

        body.getChildren().add(popupSeparator());

        // Tombol Setujui/Tolak langsung dari popup jika masih menunggu
        String status = b.getStatus() != null ? b.getStatus() : "";
        if (status.equalsIgnoreCase("Menunggu Konfirmasi")) {
            HBox aksiBox = new HBox(12);
            aksiBox.setAlignment(Pos.CENTER);

            Button btnS = new Button("✓  Setujui");
            btnS.getStyleClass().add("btn-setujui");
            btnS.setPrefWidth(140);
            btnS.setOnAction(e -> { popup.close(); handleAksi(b, "Disetujui"); });

            Button btnT = new Button("✗  Tolak");
            btnT.getStyleClass().add("btn-tolak");
            btnT.setPrefWidth(140);
            btnT.setOnAction(e -> { popup.close(); handleAksi(b, "Ditolak"); });

            aksiBox.getChildren().addAll(btnS, btnT);
            body.getChildren().add(aksiBox);
            body.getChildren().add(popupSeparator());
        }

        Button btnTutup = new Button("Tutup");
        btnTutup.getStyleClass().add("popup-btn-close");
        btnTutup.setMaxWidth(Double.MAX_VALUE);
        btnTutup.setOnAction(e -> popup.close());
        body.getChildren().add(btnTutup);

        VBox root = new VBox();
        root.getStyleClass().add("popup-detail-root");
        root.getChildren().addAll(header, body);

        Scene scene = new Scene(root, 420, 0);
        scene.getStylesheets().add(getClass().getResource("/view/kelola_pesanan.css").toExternalForm());
        popup.setScene(scene);
        popup.sizeToScene();
        popup.centerOnScreen();
        popup.showAndWait();
    }

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

    private String resolveBadgeClass(String status) {
        if (status == null) return "badge-menunggu";
        return switch (status.toLowerCase()) {
            case "disetujui" -> "badge-disetujui";
            case "ditolak"   -> "badge-ditolak";
            case "selesai"   -> "badge-selesai";
            default          -> "badge-menunggu";
        };
    }

    private void updateFilterStyle(String aktif) {
        for (Button btn : new Button[]{btnFilterSemua, btnFilterMenunggu, btnFilterDisetujui, btnFilterDitolak, btnFilterSelesai}) {
            btn.getStyleClass().setAll("filter-btn");
        }
        Button target = switch (aktif) {
            case "menunggu"  -> btnFilterMenunggu;
            case "disetujui" -> btnFilterDisetujui;
            case "ditolak"   -> btnFilterDitolak;
            case "selesai"   -> btnFilterSelesai;
            default          -> btnFilterSemua;
        };
        target.getStyleClass().setAll("filter-btn-active");
    }

    @FXML private void filterSemua()     { loadData("semua");     }
    @FXML private void filterMenunggu()  { loadData("menunggu");  }
    @FXML private void filterDisetujui() { loadData("disetujui"); }
    @FXML private void filterDitolak()   { loadData("ditolak");   }
    @FXML private void filterSelesai()   { loadData("selesai");   }

    @FXML private void handleLogout() {
        UserDAO.getInstance().logout();
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goDashboard()     { try { SceneManager.showAdminDashboard(); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goKelolaPesanan() { /* halaman ini sendiri */ }
    @FXML private void goKelolaPaket()   { try { SceneManager.showKelolaPaket(); } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goUploadGaleri()  { System.out.println("TODO: Upload Galeri"); }
    @FXML private void goKalender()      { System.out.println("TODO: Kalender"); }
    @FXML private void goPelanggan()     { System.out.println("TODO: Pelanggan"); }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}