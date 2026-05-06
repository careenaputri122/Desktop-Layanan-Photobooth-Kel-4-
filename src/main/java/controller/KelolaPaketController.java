package controller;

import dao.PaketDAO;
import dao.UserDAO;
import model.Paket;
import model.User;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class KelolaPaketController {

    @FXML private Label labelAdminInitial;
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;
    @FXML private Label labelJumlahPaket;

    @FXML private TableView<Paket> tablePaket;
    @FXML private TableColumn<Paket, Number> colId;
    @FXML private TableColumn<Paket, String> colNama;
    @FXML private TableColumn<Paket, String> colTipe;
    @FXML private TableColumn<Paket, String> colHarga;
    @FXML private TableColumn<Paket, Void> colAksi;

    @FXML private TextField fieldNama;
    @FXML private TextField fieldHarga;
    @FXML private ComboBox<String> comboTipe;
    @FXML private Button btnSimpan;
    @FXML private Button btnUpdate;
    @FXML private Button btnHapus;
    @FXML private Button btnBatal;

    private final NumberFormat rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));
    private Paket selectedPaket;

    @FXML
    public void initialize() {
        setupSidebarProfile();
        setupForm();
        setupTable();
        loadData();
        setModeTambah();
    }

    private void setupSidebarProfile() {
        User admin = UserDAO.getInstance().getCurrentUser();
        if (admin != null) {
            labelAdminName.setText(admin.getNamaDepan() + " " + admin.getNamaBelakang());
            labelAdminRole.setText("Administrator");
            labelAdminInitial.setText(String.valueOf(admin.getNamaDepan().charAt(0)).toUpperCase());
        }
    }

    private void setupForm() {
        comboTipe.setItems(FXCollections.observableArrayList("Cetak", "Tanpa Cetak"));
        comboTipe.getSelectionModel().selectFirst();

        fieldHarga.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null && !newVal.matches("\\d*")) {
                fieldHarga.setText(newVal.replaceAll("[^\\d]", ""));
            }
        });
    }

    private void setupTable() {
        tablePaket.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

        colId.setCellValueFactory(data ->
            new javafx.beans.property.SimpleIntegerProperty(data.getValue().getId())
        );
        colNama.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(nullToDash(data.getValue().getNama()))
        );
        colTipe.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(nullToDash(data.getValue().getTipe()))
        );
        colHarga.setCellValueFactory(data ->
            new javafx.beans.property.SimpleStringProperty(rupiahFmt.format(data.getValue().getHarga()))
        );

        colId.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Number id, boolean empty) {
                super.updateItem(id, empty);
                if (empty || id == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText("#" + id.intValue());
                    setStyle("-fx-text-fill: #EC4899; -fx-font-weight: bold;");
                }
            }
        });

        colTipe.setCellFactory(col -> new TableCell<>() {
            private final Label badge = new Label();
            {
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                setAlignment(Pos.CENTER_LEFT);
            }

            @Override
            protected void updateItem(String tipe, boolean empty) {
                super.updateItem(tipe, empty);
                if (empty || tipe == null) {
                    setGraphic(null);
                } else {
                    badge.setText(tipe);
                    badge.getStyleClass().setAll(
                        tipe.equalsIgnoreCase("Cetak") ? "badge-cetak" : "badge-digital"
                    );
                    setGraphic(badge);
                }
            }
        });

        colAksi.setCellFactory(col -> new TableCell<>() {
            private final Button btnEdit = new Button("Edit");
            private final Button btnDelete = new Button("Hapus");
            private final HBox box = new HBox(8, btnEdit, btnDelete);

            {
                box.setAlignment(Pos.CENTER_LEFT);
                btnEdit.getStyleClass().add("btn-table-edit");
                btnDelete.getStyleClass().add("btn-table-delete");
                btnEdit.setOnAction(e -> pilihPaket(getTableView().getItems().get(getIndex())));
                btnDelete.setOnAction(e -> hapusPaket(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        tablePaket.setRowFactory(tv -> {
            TableRow<Paket> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getClickCount() == 1 && !(event.getTarget() instanceof Button)) {
                    pilihPaket(row.getItem());
                }
            });
            return row;
        });
    }

    private void loadData() {
        List<Paket> data = PaketDAO.getInstance().findAll();
        tablePaket.setItems(FXCollections.observableArrayList(data));
        labelJumlahPaket.setText(data.size() + " paket");
    }

    @FXML
    private void simpanPaket() {
        Paket paket = buildPaketFromForm();
        if (paket == null) return;

        if (PaketDAO.getInstance().save(paket)) {
            loadData();
            clearForm();
            showInfo("Paket berhasil ditambahkan.");
        } else {
            showError("Gagal menambahkan paket.");
        }
    }

    @FXML
    private void updatePaket() {
        if (selectedPaket == null) return;

        Paket paket = buildPaketFromForm();
        if (paket == null) return;
        paket.setId(selectedPaket.getId());

        if (PaketDAO.getInstance().update(paket)) {
            loadData();
            clearForm();
            showInfo("Paket berhasil diperbarui.");
        } else {
            showError("Gagal memperbarui paket.");
        }
    }

    @FXML
    private void hapusPaketTerpilih() {
        if (selectedPaket != null) hapusPaket(selectedPaket);
    }

    private void hapusPaket(Paket paket) {
        if (paket == null) return;

        Alert alert = new Alert(
            Alert.AlertType.CONFIRMATION,
            "Hapus " + paket.getNama() + "? Paket yang sudah dipakai di pesanan tidak bisa dihapus.",
            ButtonType.YES,
            ButtonType.NO
        );
        alert.setHeaderText(null);
        alert.setTitle("Konfirmasi Hapus");
        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                if (PaketDAO.getInstance().delete(paket.getId())) {
                    loadData();
                    clearForm();
                    showInfo("Paket berhasil dihapus.");
                } else {
                    showError("Gagal menghapus paket. Kemungkinan paket sudah dipakai pada data pesanan.");
                }
            }
        });
    }

    private Paket buildPaketFromForm() {
        String nama = fieldNama.getText() != null ? fieldNama.getText().trim() : "";
        String hargaText = fieldHarga.getText() != null ? fieldHarga.getText().trim() : "";
        String tipe = comboTipe.getValue();

        if (nama.isEmpty()) {
            showError("Nama paket harus diisi.");
            fieldNama.requestFocus();
            return null;
        }
        if (hargaText.isEmpty()) {
            showError("Harga paket harus diisi.");
            fieldHarga.requestFocus();
            return null;
        }
        if (tipe == null || tipe.isBlank()) {
            showError("Tipe paket harus dipilih.");
            comboTipe.requestFocus();
            return null;
        }

        int harga;
        try {
            harga = Integer.parseInt(hargaText);
        } catch (NumberFormatException e) {
            showError("Harga harus berupa angka.");
            return null;
        }
        if (harga <= 0) {
            showError("Harga harus lebih dari 0.");
            return null;
        }

        return new Paket(0, nama, harga, tipe);
    }

    private void pilihPaket(Paket paket) {
        selectedPaket = paket;
        fieldNama.setText(paket.getNama());
        fieldHarga.setText(String.valueOf(paket.getHarga()));
        comboTipe.getSelectionModel().select(paket.getTipe());
        setModeEdit();
    }

    @FXML
    private void clearForm() {
        selectedPaket = null;
        fieldNama.clear();
        fieldHarga.clear();
        comboTipe.getSelectionModel().selectFirst();
        tablePaket.getSelectionModel().clearSelection();
        setModeTambah();
    }

    private void setModeTambah() {
        btnSimpan.setDisable(false);
        btnUpdate.setDisable(true);
        btnHapus.setDisable(true);
        btnBatal.setDisable(false);
    }

    private void setModeEdit() {
        btnSimpan.setDisable(true);
        btnUpdate.setDisable(false);
        btnHapus.setDisable(false);
        btnBatal.setDisable(false);
    }

    private String nullToDash(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR, msg, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

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

    @FXML private void goKelolaPaket() { }
    @FXML private void goUploadGaleri() { System.out.println("TODO: Upload Galeri"); }
    @FXML private void goKalender() { System.out.println("TODO: Kalender"); }
    @FXML private void goPelanggan() { System.out.println("TODO: Pelanggan"); }
}
