package controller;

import javafx.fxml.FXML;
import dao.PaketDAO;
import dao.UserDAO;
import model.Paket;
import model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class PricelistController {

    @FXML private HBox authBox;
    @FXML private VBox paketContainer;

    private final NumberFormat rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

@FXML
public void initialize() {
    setupNavbar();
    rupiahFmt.setMaximumFractionDigits(0);
    loadPaketFromDatabase();
}

private void loadPaketFromDatabase() {
    List<Paket> paketList = PaketDAO.getInstance().findAll();
    paketContainer.getChildren().clear();

    if (paketList.isEmpty()) {
        Label empty = new Label("Belum ada paket yang tersedia.");
        empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        paketContainer.getChildren().add(empty);
        return;
    }

    HBox row = null;
    for (int i = 0; i < paketList.size(); i++) {
        if (i % 2 == 0) {
            row = new HBox(20);
            row.setAlignment(Pos.CENTER);
            paketContainer.getChildren().add(row);
        }

        VBox card = createPaketCard(paketList.get(i), i);
        HBox.setHgrow(card, Priority.ALWAYS);
        row.getChildren().add(card);
    }
}

private VBox createPaketCard(Paket paket, int index) {
    VBox card = new VBox(0);
    card.getStyleClass().add("paket-card");
    card.setMaxWidth(400);

    VBox imageBox = new VBox();
    imageBox.getStyleClass().add("card-img-placeholder");
    imageBox.setAlignment(Pos.CENTER);
    imageBox.setStyle("-fx-background-color: " + resolveCardColor(index) + ";");

    Label icon = new Label(resolveCardIcon(paket, index));
    icon.setStyle("-fx-font-size: 60px;");
    imageBox.getChildren().add(icon);

    if (index == 1) {
        StackPane imageStack = new StackPane(imageBox);
        imageStack.setAlignment(Pos.TOP_RIGHT);
        Label badge = new Label("TERLARIS");
        badge.getStyleClass().add("badge-terlaris");
        badge.setStyle("-fx-translate-x: -12; -fx-translate-y: 12;");
        imageStack.getChildren().add(badge);
        card.getChildren().add(imageStack);
    } else {
        card.getChildren().add(imageBox);
    }

    VBox content = new VBox(12);
    content.setStyle("-fx-padding: 20 20 20 20;");

    Label tipe = new Label(paket.getTipe());
    tipe.getStyleClass().add(resolveBadgeClass(paket.getTipe()));

    Label nama = new Label(paket.getNama());
    nama.getStyleClass().add("paket-name");

    VBox features = new VBox(6);
    for (String feature : buildFeatures(paket)) {
        Label check = new Label("✓");
        check.getStyleClass().add("feature-check");
        Label text = new Label(feature);
        text.getStyleClass().add("feature-text");
        HBox featureRow = new HBox(8, check, text);
        featureRow.setAlignment(Pos.CENTER_LEFT);
        features.getChildren().add(featureRow);
    }

    VBox prices = new VBox(6);
    int finalPrice = paket.getHarga();
    int originalPrice = (int) Math.round(finalPrice / 0.85);

    Label original = new Label(rupiahFmt.format(originalPrice));
    original.getStyleClass().add("price-original");
    Label discount = new Label("-15%");
    discount.getStyleClass().add("price-discount-badge");
    HBox originalRow = new HBox(8, original, discount);
    originalRow.setAlignment(Pos.CENTER_LEFT);

    Label finalLabel = new Label(rupiahFmt.format(finalPrice));
    finalLabel.getStyleClass().add("price-final");
    Label member = new Label("Member");
    member.getStyleClass().add("price-member-badge");
    HBox finalRow = new HBox(8, finalLabel, member);
    finalRow.setAlignment(Pos.CENTER_LEFT);

    prices.getChildren().addAll(originalRow, finalRow);

    Button pesan = new Button("Pesan Paket");
    pesan.getStyleClass().add("btn-pesan");
    pesan.setMaxWidth(Double.MAX_VALUE);
    pesan.setOnAction(e -> pesanPaket());

    content.getChildren().addAll(tipe, nama, features, prices, pesan);
    card.getChildren().add(content);
    return card;
}

private List<String> buildFeatures(Paket paket) {
    String tipe = paket.getTipe() == null ? "" : paket.getTipe();
    if (tipe.equalsIgnoreCase("Tanpa Cetak")) {
        return List.of(
            "3 jam operasional",
            "Backdrop 2 pilihan",
            "Props standar 20 pcs",
            "Digital file semua foto",
            "Share via QR Code",
            "1 operator profesional"
        );
    }

    return List.of(
        "Operator profesional",
        "Backdrop sesuai paket",
        "Props photobooth",
        "Cetak foto sesuai paket",
        "Digital file semua foto",
        "Setup sebelum acara"
    );
}

private String resolveBadgeClass(String tipe) {
    if (tipe != null && tipe.equalsIgnoreCase("Tanpa Cetak")) return "badge-digital";
    return "badge-cetak";
}

private String resolveCardColor(int index) {
    return switch (index % 4) {
        case 1 -> "#F9D4DF";
        case 2 -> "#F5E6D0";
        case 3 -> "#E8EEFA";
        default -> "#F3E8E0";
    };
}

private String resolveCardIcon(Paket paket, int index) {
    if (paket.getTipe() != null && paket.getTipe().equalsIgnoreCase("Tanpa Cetak")) return "DIGI";
    return switch (index % 4) {
        case 1 -> "BEST";
        case 2 -> "PRO";
        case 3 -> "VIP";
        default -> "PKT";
    };
}

private void setupNavbar() {
    User user = UserDAO.getInstance().getCurrentUser();
    authBox.getChildren().clear();

    if (user != null) {
        // Sudah login → tampil nama + tombol logout
        Label namaLabel = new Label("Hi, " + user.getNamaDepan() + "!");
        namaLabel.setStyle("-fx-text-fill: #EC4899; -fx-font-weight: bold;");

        Button btnLogout = new Button("Logout");
        btnLogout.getStyleClass().add("btn-masuk");
        btnLogout.setOnAction(e -> {
            UserDAO.getInstance().logout();
            setupNavbar(); // refresh navbar
        });

        authBox.getChildren().addAll(namaLabel, btnLogout);
    } else {
        // Belum login → tampil Login + Sign in
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
    // ── Aksi Pesan ───────────────────────────────────
   @FXML private void pesanPaket() {
    if (UserDAO.getInstance().getCurrentUser() == null) {
        SceneManager.afterLoginDestination = "pemesanan";
        try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    } else {
        try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); }
    }
}

    // ── Navigasi Navbar ──────────────────────────────
    @FXML private void goHome()      { try { SceneManager.showHome();      } catch (Exception e) { e.printStackTrace(); } }
    @FXML private void goGaleri()    { try { SceneManager.showGaleri();    } catch (Exception e) { e.printStackTrace(); } }
   @FXML private void goPemesanan() {
    if (UserDAO.getInstance().getCurrentUser() == null) {
        SceneManager.afterLoginDestination = "pemesanan";
        try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    } else {
        try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); }
    }
}
    @FXML private void goAdmin()     { System.out.println("TODO: Admin");   }
    @FXML private void goMember()    { System.out.println("TODO: Member");  }
    @FXML private void goLogin()     { try { SceneManager.showLogin();      } catch (Exception e) { e.printStackTrace(); } }
    @FXML
private void goSignin() {
    try { SceneManager.showRegister(); } catch (Exception e) { e.printStackTrace(); }
}
}
