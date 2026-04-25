package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.TilePane;
import javafx.scene.control.Label;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

public class GaleriController {

    // ─── Ukuran kartu — SERAGAM untuk semua gambar ──────────────
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

    // Data: [path, kategori]
    private static final String[][] IMAGE_DATA = {
        {"/view/images/wedding.jpg",    "Wedding"},
        {"/view/images/birthday.jpg",   "Birthday"},
        {"/view/images/corporate.jpg",  "Corporate"},
        {"/view/images/wisuda.jpg",     "Wisuda"},
        {"/view/images/promo1.jpg",     "Birthday"},
        {"/view/images/promo2.jpg",     "Wedding"},
        {"/view/images/promo3.jpg",     "Wisuda"},
        {"/view/images/wedding.jpg",    "Wedding"},
        // nama alternatif jika ada
        {"/view/images/wedding1.jpg",   "Wedding"},
        {"/view/images/wedding2.jpg",   "Wedding"},
        {"/view/images/birthday1.jpg",  "Birthday"},
        {"/view/images/birthday2.jpg",  "Birthday"},
        {"/view/images/corporate1.jpg", "Corporate"},
        {"/view/images/corporate2.jpg", "Corporate"},
        {"/view/images/wisuda1.jpg",    "Wisuda"},
        {"/view/images/wisuda2.jpg",    "Wisuda"},
    };

    private String activeCategory = "Semua";

    // ─── Init ────────────────────────────────────────────────────
    @FXML
    public void initialize() {
        // Paksa TilePane menggunakan ukuran tile seragam
        galleryPane.setPrefTileWidth(CARD_WIDTH);
        galleryPane.setPrefTileHeight(CARD_HEIGHT);
        renderGallery("Semua", "");
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
            String path = data[0];
            String cat  = data[1];

            if (!category.equals("Semua") && !cat.equals(category)) continue;
            if (!keyword.isEmpty() && !cat.toLowerCase().contains(keyword)) continue;

            java.io.InputStream is = getClass().getResourceAsStream(path);
            if (is == null) continue;

            // ── ImageView ukuran seragam ─────────────────────────
            ImageView imgView = new ImageView(new Image(is));
            imgView.setFitWidth(CARD_WIDTH);
            imgView.setFitHeight(CARD_HEIGHT);
            imgView.setPreserveRatio(false);   // fill penuh, tidak ada celah kosong

            // ── Clip rounded corner pada gambar ──────────────────
            Rectangle clip = new Rectangle(CARD_WIDTH, CARD_HEIGHT);
            clip.setArcWidth(RADIUS * 2);
            clip.setArcHeight(RADIUS * 2);
            imgView.setClip(clip);

            // ── Badge kategori ───────────────────────────────────
            Label badge = new Label(cat);
            badge.getStyleClass().add("photo-category-badge");

            // ── Card container — ukuran dikunci ──────────────────
            StackPane card = new StackPane(imgView, badge);
            card.getStyleClass().add("photo-card");
            card.setPrefWidth(CARD_WIDTH);
            card.setPrefHeight(CARD_HEIGHT);
            card.setMinWidth(CARD_WIDTH);
            card.setMinHeight(CARD_HEIGHT);
            card.setMaxWidth(CARD_WIDTH);
            card.setMaxHeight(CARD_HEIGHT);

            StackPane.setAlignment(badge, Pos.BOTTOM_LEFT);
            StackPane.setMargin(badge, new Insets(0, 0, 10, 10));

            // ── Hover effect ─────────────────────────────────────
            card.setOnMouseEntered(e -> {
                card.setScaleX(1.04);
                card.setScaleY(1.04);
            });
            card.setOnMouseExited(e -> {
                card.setScaleX(1.0);
                card.setScaleY(1.0);
            });

            galleryPane.getChildren().add(card);
        }
    }

    // ─── Navigasi Navbar ─────────────────────────────────────────
    @FXML private void goHome()      { nav(() -> SceneManager.showHome());      }
    @FXML private void goPricelist() { nav(() -> SceneManager.showPricelist()); }
    @FXML private void goGaleri()    { nav(() -> SceneManager.showGaleri());    }
    @FXML private void goPemesanan() { System.out.println("TODO: Pemesanan");   }
    @FXML private void goAdmin()     { System.out.println("TODO: Admin");       }
    @FXML private void goMember()    { System.out.println("TODO: Member");      }
    @FXML private void goLogin()     { nav(() -> SceneManager.showLogin());     }

    @FunctionalInterface interface Nav { void go() throws Exception; }
    private void nav(Nav fn) { try { fn.go(); } catch (Exception e) { e.printStackTrace(); } }
}