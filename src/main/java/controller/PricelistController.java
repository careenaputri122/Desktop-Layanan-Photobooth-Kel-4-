package controller;

import javafx.fxml.FXML;
import dao.BookingDAO;
import dao.PaketDAO;
import dao.UserDAO;
import model.Paket;
import model.User;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public class PricelistController {

    @FXML private HBox authBox;
    @FXML private VBox paketContainer;

    private static final int CARDS_PER_ROW = 3;
    private final NumberFormat rupiahFmt = NumberFormat.getCurrencyInstance(new Locale("id", "ID"));

@FXML
public void initialize() {
    setupNavbar();
    rupiahFmt.setMaximumFractionDigits(0);
    loadPaketFromDatabase();
}

private void loadPaketFromDatabase() {
    List<Paket> paketList = PaketDAO.getInstance().findAll();
    boolean diskonMemberAktif = UserDAO.getInstance().currentUserHasMemberDiscount();
    paketContainer.getChildren().clear();

    if (paketList.isEmpty()) {
        Label empty = new Label("Belum ada paket yang tersedia.");
        empty.setStyle("-fx-font-size: 14px; -fx-text-fill: #6B7280;");
        paketContainer.getChildren().add(empty);
        return;
    }

    HBox row = null;
    for (int i = 0; i < paketList.size(); i++) {
        if (i % CARDS_PER_ROW == 0) {
            row = new HBox(16);
            row.setAlignment(Pos.CENTER);
            paketContainer.getChildren().add(row);
        }

        VBox card = createPaketCard(paketList.get(i), diskonMemberAktif);
        HBox.setHgrow(card, Priority.ALWAYS);
        row.getChildren().add(card);
    }
}

private VBox createPaketCard(Paket paket, boolean diskonMemberAktif) {
    VBox card = new VBox(0);
    card.getStyleClass().add("paket-card");
    card.setMaxWidth(340);

    VBox content = new VBox(10);
    content.setStyle("-fx-padding: 16 16 16 16;");

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
    int hargaNormal = paket.getHarga();

    if (diskonMemberAktif) {
        int diskon = (int) Math.round(hargaNormal * BookingDAO.MEMBER_DISCOUNT_RATE);
        int hargaMember = hargaNormal - diskon;

        Label original = new Label(rupiahFmt.format(hargaNormal));
        original.getStyleClass().add("price-original");
        Label discount = new Label("-15%");
        discount.getStyleClass().add("price-discount-badge");
        HBox originalRow = new HBox(8, original, discount);
        originalRow.setAlignment(Pos.CENTER_LEFT);

        Label finalLabel = new Label(rupiahFmt.format(hargaMember));
        finalLabel.getStyleClass().add("price-final");
        Label member = new Label("Member");
        member.getStyleClass().add("price-member-badge");
        HBox finalRow = new HBox(8, finalLabel, member);
        finalRow.setAlignment(Pos.CENTER_LEFT);

        prices.getChildren().addAll(originalRow, finalRow);
    } else {
        Label finalLabel = new Label(rupiahFmt.format(hargaNormal));
        finalLabel.getStyleClass().add("price-final");
        prices.getChildren().add(finalLabel);
    }

    Button pesan = new Button("Pesan Paket");
    pesan.getStyleClass().add("btn-pesan");
    pesan.setMaxWidth(Double.MAX_VALUE);
    pesan.setOnAction(e -> pesanPaket());

    content.getChildren().addAll(tipe, nama, features, prices, pesan);
    card.getChildren().add(content);
    return card;
}

private List<String> buildFeatures(Paket paket) {
    String keterangan = paket.getKeterangan();
    if (keterangan != null && !keterangan.isBlank()) {
        String normalized = keterangan.trim();
        String[] rawItems = normalized.contains("\n")
            ? normalized.split("\\R+")
            : normalized.split("\\s*[;,]\\s*");

        List<String> items = new ArrayList<>();
        Arrays.stream(rawItems)
            .map(String::trim)
            .filter(item -> !item.isBlank())
            .forEach(items::add);

        if (!items.isEmpty()) return items;
    }

    // Fallback: default berdasarkan tipe jika keterangan belum diisi admin
    return getDefaultFeatures(paket.getTipe());
}

private List<String> getDefaultFeatures(String tipe) {
    if (tipe != null && tipe.equalsIgnoreCase("Tanpa Cetak")) {
        return List.of(
            "2 jam operasional",
            "Backdrop 1 pilihan",
            "Props standar 10 pcs",
            "Digital file semua foto",
            "Share via QR Code",
            "1 operator profesional"
        );
    }
    return List.of(
        "4 jam operasional",
        "Backdrop 3 pilihan",
        "Props standar + tematik 30 pcs",
        "Cetak foto 4R unlimited",
        "Digital file + Share via QR Code",
        "1 operator profesional"
    );
}

private String resolveBadgeClass(String tipe) {
    if (tipe != null && tipe.equalsIgnoreCase("Tanpa Cetak")) return "badge-digital";
    return "badge-cetak";
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
            loadPaketFromDatabase();
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
