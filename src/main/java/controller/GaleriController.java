package controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.layout.VBox;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import dao.UserDAO;
import dao.GaleriDAO;
import model.User;
import java.io.File;
import java.io.InputStream;

public class GaleriController {

    @FXML private HBox authBox;

    // ─── Ukuran kartu ────────────────────────────────────────────
    private static final double CARD_WIDTH  = 260;
    private static final double CARD_HEIGHT = 195;
    private static final double RADIUS      = 14;

    @FXML private TilePane  galleryPane;
    @FXML private TextField searchField;

    @FXML private Button btnSemua;
    @FXML private Button btnWedding;
    @FXML private Button btnBirthday;
    @FXML private Button btnCorporate;
    @FXML private Button btnWisuda;

    // Data statis sebagai fallback / sample
    private static final String[][] IMAGE_DATA = {
        {"/view/images/wedding.jpg",    "Wedding",   "Wedding Leddy & Cortis",              "12 Apr 2026", "148 foto", "https://waldophotos.app.link/wZUEpqCXI2b?source=yxe67GemPpujh3uejP99tG"},
        {"/view/images/birthday.jpg",   "Birthday",  "Birthday Dea Amalia ke-19",            "20 Mar 2026", "95 foto",  "https://waldophotos.app.link/lO1N452ZI2b?source=eK1FJznkyxPNjA3oNNBYsr"},
        {"/view/images/corporate.jpg",  "Corporate", "Gathering PT. Dea Keren",             "05 Mar 2026", "210 foto", "https://waldophotos.app.link/ySkVmtc1I2b?source=nch63kSqdCv94UGPEzfKYX"},
        {"/view/images/wisuda.jpg",     "Wisuda",    "Wisuda Universitas Sriwijaya",         "5 Mar 2026",  "150 foto", "https://waldophotos.app.link/BoN6r55YI2b?source=fz3qgF1LStUT5dez1NsURm"},
        {"/view/images/birthday2.jpg",  "Birthday",  "Birthday Aluna ke-18",                 "3 Mar 2026",  "102 foto", "https://waldophotos.app.link/iY6dQTRZI2b?source=bvE5W3zQKTJECDbfPsCo2V"},
        {"/view/images/wedding2.jpg",   "Wedding",   "Wedding Tasya & Arga",                 "1 Mar 2026",  "160 foto", "https://waldophotos.app.link/OemWzcs0I2b?source=22FEpfdGDmC74MxnQJ9wSoZ"},
        {"/view/images/wisuda2.jpg",    "Wisuda",    "Wisuda Politeknik Negeri Sriwijaya",   "26 Feb 2026", "135 foto", "https://waldophotos.app.link/u3zNxhrZI2b?source=nvet8asMZTVEYkHr3oEpsi"},
        {"/view/images/wedding3.jpg",   "Wedding",   "Wedding Tiara & Fauzan",               "22 Feb 2026", "175 foto", "https://waldophotos.app.link/mexEMrN0I2b?source=vpb8c1KAqcGyEU6cP8Bgt5"},
    };

    private String activeCategory = "Semua";

    // ─── Init ────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        galleryPane.setPrefTileWidth(CARD_WIDTH);
        galleryPane.setPrefTileHeight(CARD_HEIGHT);
        setupNavbar();
        Platform.runLater(() -> renderGallery("Semua", ""));
    }

    // ─── Setup Navbar ─────────────────────────────────────────────
    private void setupNavbar() {
        User user = UserDAO.getInstance().getCurrentUser();
        authBox.getChildren().clear();

        if (user != null) {
            Label namaLabel = new Label("Hi, " + user.getNamaDepan() + "!");
            namaLabel.setStyle("-fx-text-fill: #EC4899; -fx-font-weight: bold;");

            Button btnLogout = new Button("Logout");
            btnLogout.getStyleClass().add("btn-masuk");
            btnLogout.setOnAction(e -> {
                UserDAO.getInstance().logout();
                setupNavbar();
            });

            authBox.getChildren().addAll(namaLabel, btnLogout);
        } else {
            Button btnLogin = new Button("Login");
            btnLogin.getStyleClass().add("btn-masuk");
            btnLogin.setOnAction(e -> {
                try { SceneManager.showLogin(); } catch (Exception ex) { ex.printStackTrace(); }
            });

            Button btnSignin = new Button("Sign in");
            btnSignin.getStyleClass().add("btn-masuk");
            btnSignin.setOnAction(e -> {
                try { SceneManager.showRegister(); } catch (Exception ex) { ex.printStackTrace(); }
            });

            authBox.getChildren().addAll(btnLogin, btnSignin);
        }
    }

    // ─── Filter Actions ──────────────────────────────────────────
    @FXML private void filterSemua()     { setActiveFilter("Semua",     btnSemua);     }
    @FXML private void filterWedding()   { setActiveFilter("Wedding",   btnWedding);   }
    @FXML private void filterBirthday()  { setActiveFilter("Birthday",  btnBirthday);  }
    @FXML private void filterCorporate() { setActiveFilter("Corporate", btnCorporate); }
    @FXML private void filterWisuda()    { setActiveFilter("Wisuda",    btnWisuda);    }

    @FXML
    private void onSearch() {
        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        renderGallery(activeCategory, kw);
    }

    // ─── Ganti tombol aktif ──────────────────────────────────────
    private void setActiveFilter(String category, Button clicked) {
        activeCategory = category;

        for (Button b : new Button[]{btnSemua, btnWedding, btnBirthday, btnCorporate, btnWisuda}) {
            if (b == null) continue;
            b.getStyleClass().setAll("filter-btn");
        }
        if (clicked != null) clicked.getStyleClass().setAll("filter-active");

        String kw = searchField != null ? searchField.getText().trim().toLowerCase() : "";
        renderGallery(category, kw);
    }

    // ─── Load image dari path (classpath atau file absolut) ───────
    /**
     * Mencoba memuat gambar dari:
     * 1. Path absolut file (foto yang diupload admin)
     * 2. Classpath resource (foto statis bawaan)
     * Mengembalikan null jika tidak berhasil.
     */
    private Image loadImage(String path) {
        if (path == null || path.isEmpty()) return null;

        // Coba sebagai file absolut di disk terlebih dahulu
        File f = new File(path);
        if (f.exists() && f.isFile()) {
            try {
                return new Image(f.toURI().toString(), CARD_WIDTH, CARD_HEIGHT, true, true);
            } catch (Exception e) { /* lanjut ke classpath */ }
        }

        // Coba sebagai classpath resource
        InputStream is = getClass().getResourceAsStream(path);
        if (is != null) {
            try {
                return new Image(is);
            } catch (Exception e) { /* gagal */ }
        }

        return null;
    }

    // ─── Render gallery ─────────────────────────────────────────
    private void renderGallery(String category, String keyword) {
        galleryPane.getChildren().clear();

        java.util.List<String[]> allData = new java.util.ArrayList<>();

        // 1. Data dari database (foto yang diupload admin) — ditampilkan LEBIH DULU
        try {
            java.util.List<String[]> dbData = GaleriDAO.getInstance().findAll();
            // dbData: [id, judul, tema, tanggal_event, jumlah_foto, link_album, file_path]
            for (String[] d : dbData) {
                allData.add(new String[]{
                    d[6],                  // file_path (path absolut)
                    d[2],                  // tema/kategori
                    d[1],                  // judul
                    d[3],                  // tanggal
                    d[4] + " foto",        // jumlah foto
                    d[5]                   // link album
                });
            }
        } catch (Exception e) { /* DB tidak tersedia, lanjut ke data statis */ }

        // 2. Data statis (bawaan) sebagai fallback / sample
        for (String[] d : IMAGE_DATA) allData.add(d);

        for (String[] data : allData) {
            String path  = data[0];
            String cat   = data[1];
            String judul = data[2];
            String tgl   = data[3];
            String nFoto = data[4];
            String link  = data[5];

            if (!category.equals("Semua") && !cat.equals(category)) continue;
            if (!keyword.isEmpty() && !cat.toLowerCase().contains(keyword)
                    && !judul.toLowerCase().contains(keyword)) continue;

            // Load gambar dari path absolut atau classpath
            Image img = loadImage(path);
            if (img == null) continue; // skip jika gambar tidak ditemukan

            // ── Gambar ──────────────────────────────────────────────
            ImageView imgView = new ImageView(img);
            imgView.setFitWidth(CARD_WIDTH);
            imgView.setFitHeight(CARD_HEIGHT);
            imgView.setPreserveRatio(true);

            Rectangle clip = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            clip.setArcWidth(RADIUS * 2);
            clip.setArcHeight(RADIUS * 2);
            imgView.setClip(clip);

            // ── Badge kategori ───────────────────────────────────────
            Label badge = new Label(cat);
            badge.getStyleClass().add("photo-category-badge");

            // ── Overlay bawah ────────────────────────────────────────
            Label lblJudul = new Label(judul);
            lblJudul.setStyle(
                "-fx-font-weight: bold;" +
                "-fx-font-size: 13px;" +
                "-fx-text-fill: white;"
            );
            lblJudul.setWrapText(true);
            lblJudul.setMaxWidth(CARD_WIDTH - 20);

            Label lblInfo = new Label(tgl + " • " + nFoto);
            lblInfo.setStyle(
                "-fx-font-size: 11px;" +
                "-fx-text-fill: rgba(255,255,255,0.85);"
            );

            VBox overlayBox = new VBox(4, badge, lblJudul, lblInfo);
            overlayBox.setStyle(
                "-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.0) 100%);" +
                "-fx-padding: 10 10 10 10;" +
                "-fx-background-radius: 0 0 14 14;"
            );
            overlayBox.setPrefWidth(CARD_WIDTH);
            overlayBox.setMaxWidth(CARD_WIDTH);
            overlayBox.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

            // ── Card ─────────────────────────────────────────────────
            StackPane card = new StackPane(imgView, overlayBox);
            card.setPrefWidth(CARD_WIDTH);
            card.setPrefHeight(CARD_HEIGHT);
            card.setMinWidth(CARD_WIDTH);
            card.setMinHeight(CARD_HEIGHT);
            card.setMaxWidth(CARD_WIDTH);
            card.setMaxHeight(CARD_HEIGHT);
            card.getStyleClass().add("photo-card");

            StackPane.setAlignment(overlayBox, Pos.BOTTOM_CENTER);

            card.setOnMouseEntered(e -> { card.setScaleX(1.04); card.setScaleY(1.04); });
            card.setOnMouseExited(e ->  { card.setScaleX(1.0);  card.setScaleY(1.0);  });
            card.setOnMouseClicked(e -> {
                if (link != null && !link.isEmpty()) {
                    try {
                        java.awt.Desktop.getDesktop().browse(new java.net.URI(link));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
            });

            galleryPane.getChildren().add(card);
        }
    }

    // ─── Navigasi Navbar ─────────────────────────────────────────
    @FXML private void goHome()      { nav(() -> SceneManager.showHome());      }
    @FXML private void goPricelist() { nav(() -> SceneManager.showPricelist()); }
    @FXML private void goGaleri()    { nav(() -> SceneManager.showGaleri());    }
    @FXML private void goPemesanan() {
        if (UserDAO.getInstance().getCurrentUser() == null) {
            SceneManager.afterLoginDestination = "pemesanan";
            try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
        } else {
            try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); }
        }
    }
    @FXML private void goSignin() { nav(() -> SceneManager.showRegister()); }
    @FXML private void goLogin()  { nav(() -> SceneManager.showLogin());    }

    @FunctionalInterface interface Nav { void go() throws Exception; }
    private void nav(Nav fn) { try { fn.go(); } catch (Exception e) { e.printStackTrace(); } }
}
