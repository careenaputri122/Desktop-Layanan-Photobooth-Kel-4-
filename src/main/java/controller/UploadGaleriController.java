package controller;

import dao.GaleriDAO;
import dao.UserDAO;
import model.User;

import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;

import java.io.*;
import java.nio.file.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * UploadGaleriController — Halaman Upload Galeri Admin.
 *
 * Fitur:
 *  - Form upload: pilih gambar, judul, tema, tanggal, jumlah foto, link album
 *  - Gambar disalin ke folder resources/view/images/galeri/
 *  - Data disimpan ke tabel `galeri` di database
 *  - Grid galeri menampilkan semua foto yang sudah diupload
 *  - Filter per tema & hapus foto
 */
public class UploadGaleriController {

    // ── Sidebar ───────────────────────────────────────────────────
    @FXML private Label labelAdminName;
    @FXML private Label labelAdminRole;
    @FXML private Label labelAdminInitial;

    // ── Form Upload ───────────────────────────────────────────────
    @FXML private ImageView previewImage;
    @FXML private Label     labelNamaFile;
    @FXML private TextField fieldJudul;
    @FXML private ComboBox<String> comboTema;
    @FXML private TextField fieldTanggal;
    @FXML private TextField fieldJumlahFoto;
    @FXML private TextField fieldLinkAlbum;
    @FXML private Label     labelStatus;

    // ── Filter & Grid ─────────────────────────────────────────────
    @FXML private ComboBox<String> comboFilter;
    @FXML private TilePane  galleryPane;
    @FXML private Label     labelJumlahFotoDb;

    // ── State ─────────────────────────────────────────────────────
    private File selectedFile   = null;
    private String savedPath    = null;

    private static final double CARD_W = 220;
    private static final double CARD_H = 160;

    private static final String[] TEMA_LIST = {
        "Wedding", "Birthday", "Corporate", "Wisuda", "Lainnya"
    };

    private static final DateTimeFormatter DATE_FMT =
        DateTimeFormatter.ofPattern("d MMM yyyy", new Locale("id", "ID"));

    // ── Init ──────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        setupSidebar();
        comboTema.getItems().addAll(TEMA_LIST);
        comboTema.setValue("Wedding");

        comboFilter.getItems().add("Semua");
        comboFilter.getItems().addAll(TEMA_LIST);
        comboFilter.setValue("Semua");
        comboFilter.setOnAction(e -> loadGallery());

        // Default tanggal hari ini
        fieldTanggal.setText(LocalDate.now().format(DATE_FMT));

        loadGallery();
    }

    private void setupSidebar() {
        User admin = UserDAO.getInstance().getCurrentUser();
        if (admin != null) {
            labelAdminName.setText(admin.getNamaDepan() + " " + admin.getNamaBelakang());
            labelAdminRole.setText("Administrator");
            labelAdminInitial.setText(
                String.valueOf(admin.getNamaDepan().charAt(0)).toUpperCase());
        }
    }

    // ── Pilih Gambar ──────────────────────────────────────────────
    @FXML
    private void handlePilihGambar() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Pilih Foto");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Gambar", "*.jpg", "*.jpeg", "*.png", "*.webp"));

        File file = fc.showOpenDialog(
            galleryPane.getScene() != null ? galleryPane.getScene().getWindow() : null);
        if (file == null) return;

        selectedFile = file;
        savedPath = null;
        labelNamaFile.setText(file.getName());

        try {
            Image img = new Image(file.toURI().toString(),
                                  240, 160, true, true, false);
            previewImage.setImage(img);
            previewImage.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
        setStatus("", false);
    }

    // ── Upload / Simpan ───────────────────────────────────────────
    @FXML
    private void handleUpload() {
        // Validasi input
        String judul     = fieldJudul.getText().trim();
        String tema      = comboTema.getValue();
        String tanggal   = fieldTanggal.getText().trim();
        String jumlahStr = fieldJumlahFoto.getText().trim();
        String link      = fieldLinkAlbum.getText().trim();

        if (selectedFile == null) {
            setStatus("⚠ Pilih gambar terlebih dahulu.", true);
            return;
        }
        if (judul.isEmpty()) {
            setStatus("⚠ Judul tidak boleh kosong.", true);
            return;
        }
        if (tema == null || tema.isEmpty()) {
            setStatus("⚠ Pilih tema.", true);
            return;
        }
        if (tanggal.isEmpty()) {
            setStatus("⚠ Isi tanggal event.", true);
            return;
        }
        int jumlahFoto = 0;
        if (!jumlahStr.isEmpty()) {
            try { jumlahFoto = Integer.parseInt(jumlahStr); }
            catch (NumberFormatException e) {
                setStatus("⚠ Jumlah foto harus berupa angka.", true);
                return;
            }
        }

        // Salin gambar ke folder galeri
        try {
            // Resolusi ke folder resources di classpath (dalam project)
            java.net.URL resUrl = getClass().getResource("/view/images/galeri");
            Path destDir;
            if (resUrl != null) {
                destDir = Paths.get(resUrl.toURI());
            } else {
                // Fallback: buat folder di sebelah jar / working directory
                destDir = Paths.get("images", "galeri");
                Files.createDirectories(destDir);
            }

            String ext      = getExtension(selectedFile.getName());
            String fileName = "galeri_" + System.currentTimeMillis() + "." + ext;
            Path   dest     = destDir.resolve(fileName);
            Files.copy(selectedFile.toPath(), dest, StandardCopyOption.REPLACE_EXISTING);
            savedPath = "/view/images/galeri/" + fileName;

        } catch (Exception e) {
            // Simpan hanya path absolut sebagai fallback
            savedPath = selectedFile.getAbsolutePath();
        }

        // Simpan ke database
        boolean ok = GaleriDAO.getInstance().save(
            judul, tema, tanggal, jumlahFoto, link, savedPath);

        if (ok) {
            setStatus("✓ Foto berhasil diupload ke galeri!", false);
            resetForm();
            loadGallery();
        } else {
            setStatus("✗ Gagal menyimpan ke database. Coba lagi.", true);
        }
    }

    // ── Reset Form ────────────────────────────────────────────────
    private void resetForm() {
        selectedFile = null;
        savedPath    = null;
        fieldJudul.clear();
        comboTema.setValue("Wedding");
        fieldTanggal.setText(LocalDate.now().format(DATE_FMT));
        fieldJumlahFoto.clear();
        fieldLinkAlbum.clear();
        labelNamaFile.setText("Belum ada file dipilih");
        previewImage.setImage(null);
        previewImage.setVisible(false);
    }

    // ── Load Galeri dari DB ───────────────────────────────────────
    private void loadGallery() {
        galleryPane.getChildren().clear();
        String filter = comboFilter.getValue();

        List<String[]> rows = "Semua".equals(filter)
            ? GaleriDAO.getInstance().findAll()
            : GaleriDAO.getInstance().findByTema(filter);

        if (labelJumlahFotoDb != null) {
            labelJumlahFotoDb.setText(rows.size() + " foto");
        }

        for (String[] row : rows) {
            // row: [id, judul, tema, tanggal, jumlahFoto, link, filePath]
            galleryPane.getChildren().add(buildCard(row));
        }
    }

    // ── Build Kartu Galeri ────────────────────────────────────────
    private VBox buildCard(String[] row) {
        String id        = row[0];
        String judul     = row[1];
        String tema      = row[2];
        String tanggal   = row[3];
        String nFoto     = row[4];
        String link      = row[5];
        String filePath  = row[6];

        // ── Gambar ──────────────────────────────────────
        ImageView iv = new ImageView();
        iv.setFitWidth(CARD_W);
        iv.setFitHeight(CARD_H);
        iv.setPreserveRatio(false);

        Image img = loadImage(filePath);
        if (img != null) iv.setImage(img);

        Rectangle clip = new Rectangle(CARD_W, CARD_H);
        clip.setArcWidth(20); clip.setArcHeight(20);
        iv.setClip(clip);

        // ── Overlay teks ────────────────────────────────
        Label badgeTema = new Label(tema);
        badgeTema.setStyle(
            "-fx-background-color: #EC4899;" +
            "-fx-text-fill: white;" +
            "-fx-font-size: 9px; -fx-font-weight: bold;" +
            "-fx-background-radius: 4; -fx-padding: 2 7 2 7;");

        Label lblJudul = new Label(judul);
        lblJudul.setStyle("-fx-font-weight: bold; -fx-font-size: 11px; -fx-text-fill: white;");
        lblJudul.setWrapText(true);
        lblJudul.setMaxWidth(CARD_W - 16);

        Label lblInfo = new Label(tanggal + " • " + nFoto + " foto");
        lblInfo.setStyle("-fx-font-size: 10px; -fx-text-fill: rgba(255,255,255,0.85);");

        VBox overlay = new VBox(3, badgeTema, lblJudul, lblInfo);
        overlay.setStyle(
            "-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.72) 0%, rgba(0,0,0,0) 100%);" +
            "-fx-padding: 8 8 8 8; -fx-background-radius: 0 0 10 10;");
        overlay.setPrefWidth(CARD_W);

        // ── Tombol hapus ─────────────────────────────────
        Button btnHapus = new Button("✕");
        btnHapus.setStyle(
            "-fx-background-color: rgba(220,38,38,0.85);" +
            "-fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold;" +
            "-fx-background-radius: 12; -fx-cursor: hand;" +
            "-fx-min-width: 24; -fx-min-height: 24;" +
            "-fx-max-width: 24; -fx-max-height: 24; -fx-padding: 0;");
        btnHapus.setOnAction(e -> handleHapus(id, judul));

        // ── Tombol buka link ─────────────────────────────
        Button btnLink = null;
        if (!link.isEmpty()) {
            btnLink = new Button("🔗");
            btnLink.setStyle(
                "-fx-background-color: rgba(236,72,153,0.85);" +
                "-fx-text-fill: white; -fx-font-size: 11px;" +
                "-fx-background-radius: 12; -fx-cursor: hand;" +
                "-fx-min-width: 24; -fx-min-height: 24;" +
                "-fx-max-width: 24; -fx-max-height: 24; -fx-padding: 0;");
            final String linkFinal = link;
            btnLink.setOnAction(e -> {
                try {
                    java.awt.Desktop.getDesktop().browse(new java.net.URI(linkFinal));
                } catch (Exception ex) { ex.printStackTrace(); }
            });
        }

        HBox btnBox = new HBox(4);
        btnBox.setAlignment(Pos.TOP_RIGHT);
        btnBox.setPadding(new Insets(6, 6, 0, 0));
        btnBox.getChildren().add(btnHapus);
        if (btnLink != null) btnBox.getChildren().add(btnLink);

        // ── Stack card ───────────────────────────────────
        StackPane stack = new StackPane(iv, overlay, btnBox);
        stack.setPrefWidth(CARD_W); stack.setPrefHeight(CARD_H);
        stack.setMaxWidth(CARD_W);  stack.setMaxHeight(CARD_H);
        StackPane.setAlignment(overlay, Pos.BOTTOM_CENTER);
        StackPane.setAlignment(btnBox,  Pos.TOP_RIGHT);

        // hover
        stack.setOnMouseEntered(e -> { stack.setScaleX(1.03); stack.setScaleY(1.03); });
        stack.setOnMouseExited(e  -> { stack.setScaleX(1.0);  stack.setScaleY(1.0);  });

        VBox card = new VBox(stack);
        card.setPrefWidth(CARD_W);
        card.setStyle("-fx-background-radius: 10; -fx-cursor: hand;");
        return card;
    }

    // ── Hapus dari DB ─────────────────────────────────────────────
    private void handleHapus(String idStr, String judul) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Hapus Foto");
        confirm.setHeaderText("Hapus foto \"" + judul + "\"?");
        confirm.setContentText("Data akan dihapus dari galeri. File gambar tidak akan ikut terhapus.");

        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                boolean ok = GaleriDAO.getInstance().delete(Integer.parseInt(idStr));
                if (ok) {
                    setStatus("✓ Foto \"" + judul + "\" dihapus.", false);
                    loadGallery();
                } else {
                    setStatus("✗ Gagal menghapus foto.", true);
                }
            }
        });
    }

    // ── Helper: load gambar dari path (classpath atau absolut) ────
    private Image loadImage(String path) {
        if (path == null || path.isEmpty()) return null;
        // Coba classpath dulu (untuk gambar yang sudah ada di resources)
        java.io.InputStream is = getClass().getResourceAsStream(path);
        if (is != null) return new Image(is);
        // Coba sebagai file absolut
        try {
            File f = new File(path);
            if (f.exists()) return new Image(f.toURI().toString());
        } catch (Exception e) { /* ignored */ }
        return null;
    }

    private String getExtension(String name) {
        int dot = name.lastIndexOf('.');
        return dot >= 0 ? name.substring(dot + 1).toLowerCase() : "jpg";
    }

    private void setStatus(String msg, boolean isError) {
        if (labelStatus == null) return;
        labelStatus.setText(msg);
        labelStatus.setStyle(isError
            ? "-fx-text-fill: #DC2626; -fx-font-size: 12px;"
            : "-fx-text-fill: #059669; -fx-font-size: 12px;");
    }

    // ── Navigasi Sidebar ──────────────────────────────────────────
    @FXML private void handleLogout() {
        UserDAO.getInstance().logout();
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }
    @FXML private void goDashboard()     { nav(SceneManager::showAdminDashboard);   }
    @FXML private void goKelolaPesanan() { nav(SceneManager::showKelolaPesanan);    }
    @FXML private void goKelolaPaket()   { nav(SceneManager::showKelolaPaket);      }
    @FXML private void goUploadGaleri()  { nav(SceneManager::showUploadGaleri);     }
    @FXML private void goKalender()      { nav(SceneManager::showKalenderBooking);  }
    @FXML private void goPelanggan()     { nav(SceneManager::showKelolaPelanggan);  }

    @FunctionalInterface interface Nav { void go() throws Exception; }
    private void nav(Nav fn) { try { fn.go(); } catch (Exception e) { e.printStackTrace(); } }
}
