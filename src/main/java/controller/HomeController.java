package controller;

import javafx.fxml.FXML;
import dao.UserDAO;
import model.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class HomeController {

    @FXML private HBox authBox;

@FXML
public void initialize() {
    setupNavbar();
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

    @FXML
    private void goHome() {
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goPricelist() {
        try { SceneManager.showPricelist(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goGaleri() {
        try { SceneManager.showGaleri(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML private void goPemesanan() {
    if (UserDAO.getInstance().getCurrentUser() == null) {
        SceneManager.afterLoginDestination = "pemesanan";
        try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    } else {
        try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); }
    }
}

    @FXML
    private void goAdmin() {
        // TODO: SceneManager.showAdmin();
        System.out.println("Navigasi ke Admin");
    }

    @FXML
    private void goMember() {
        // TODO: SceneManager.showMember();
        System.out.println("Navigasi ke Member");
    }

    @FXML
    private void goLogin() {
        try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goSignin() {
        try { SceneManager.showRegister(); } catch (Exception e) { e.printStackTrace(); }
    }

    // ── Aksi Konten ──────────────────────────────────
    @FXML
    private void ambilPromo() {
        goPemesanan();
    }

    @FXML
    private void openWhatsApp() {
        try {
            java.awt.Desktop.getDesktop().browse(
                new java.net.URI("https://wa.me/6281234567890?text=Halo+Aksaf+Photobooth,+saya+mau+tanya+tentang+paket")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
