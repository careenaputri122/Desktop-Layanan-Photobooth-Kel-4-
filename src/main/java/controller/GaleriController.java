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
import javafx.geometry.Insets;
import dao.UserDAO;
import model.User;

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

    private static final String[][] IMAGE_DATA = {
        {"/view/images/wedding.jpg",    "Wedding",   "Wedding Leddy & Cortis",              "12 Apr 2026", "148 foto"},
        {"/view/images/birthday.jpg",   "Birthday",  "Birthday Dea Amalia ke-5",            "20 Mar 2026", "95 foto"},
        {"/view/images/corporate.jpg",  "Corporate", "Gathering PT. Dea Keren",             "05 Mar 2026", "210 foto"},
        {"/view/images/wisuda.jpg",     "Wisuda",    "Wisuda Universitas Sriwijaya",        "5 Mar 2026",  "150 foto"},
        {"/view/images/birthday2.jpg",  "Birthday",  "Birthday Aluna ke-18",                "3 Mar 2026",  "102 foto"},
        {"/view/images/wedding2.jpg",   "Wedding",   "Wedding Hana & Rizky",                "1 Mar 2026",  "160 foto"},
        {"/view/images/wisuda2.jpg",    "Wisuda",    "Wisuda Politeknik Negeri Sriwijaya",  "26 Feb 2026", "135 foto"},
        {"/view/images/wedding3.jpg",   "Wedding",   "Wedding Tiara & Fauzan",              "22 Feb 2026", "175 foto"}
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

    // ─── Render gallery ──────────────────────────────────────────
    private void renderGallery(String category, String keyword) {
        galleryPane.getChildren().clear();

        for (String[] data : IMAGE_DATA) {
            String path  = data[0];
            String cat   = data[1];
            String judul = data[2];
            String tgl   = data[3];
            String nFoto = data[4];

            if (!category.equals("Semua") && !cat.equals(category)) continue;
            if (!keyword.isEmpty() && !cat.toLowerCase().contains(keyword)
                    && !judul.toLowerCase().contains(keyword)) continue;

            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is == null) continue;

            // ── Gambar ──────────────────────────────────────────────
            ImageView imgView = new ImageView(new Image(is));
            imgView.setFitWidth(CARD_WIDTH);
            imgView.setFitHeight(CARD_HEIGHT);
            imgView.setPreserveRatio(false);

            Rectangle clip = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            clip.setArcWidth(RADIUS * 2);
            clip.setArcHeight(RADIUS * 2);
            imgView.setClip(clip);

            // ── Badge kategori ───────────────────────────────────────
            Label badge = new Label(cat);
            badge.getStyleClass().add("photo-category-badge");

            // ── Overlay bawah (badge + judul + info) ─────────────────
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

            // badge di dalam overlayBox, di atas judul
            VBox overlayBox = new VBox(4, badge, lblJudul, lblInfo);
            overlayBox.setStyle(
                "-fx-background-color: linear-gradient(to top, rgba(0,0,0,0.7) 0%, rgba(0,0,0,0.0) 100%);" +
                "-fx-padding: 10 10 10 10;" +
                "-fx-background-radius: 0 0 14 14;"
            );
            overlayBox.setPrefWidth(CARD_WIDTH);
            overlayBox.setMaxWidth(CARD_WIDTH);
            overlayBox.setMaxHeight(javafx.scene.layout.Region.USE_PREF_SIZE);

            // ── Card — hanya imgView + overlayBox ─────────────────────
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
    @FXML private void goSignin()    { nav(() -> SceneManager.showRegister());  }
    @FXML private void goLogin()     { nav(() -> SceneManager.showLogin());     }

    @FunctionalInterface interface Nav { void go() throws Exception; }
    private void nav(Nav fn) { try { fn.go(); } catch (Exception e) { e.printStackTrace(); } }
}