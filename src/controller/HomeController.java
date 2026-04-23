package controller;

import javafx.fxml.FXML;

public class HomeController {

    // ── Navigasi Navbar ──────────────────────────────
    @FXML
    private void goHome() {
        try { SceneManager.showHome(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void goPricelist() {
        // TODO: SceneManager.showPricelist();
        System.out.println("Navigasi ke Pricelist");
    }

    @FXML
    private void goGaleri() {
        // TODO: SceneManager.showGaleri();
        System.out.println("Navigasi ke Galeri");
    }

    @FXML
    private void goPemesanan() {
        // TODO: SceneManager.showPemesanan();
        System.out.println("Navigasi ke Pemesanan");
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
        System.out.println("Ambil Promo diklik");
        // TODO: buka detail promo / arahkan ke pemesanan
    }

    @FXML
    private void openWhatsApp() {
        try {
            java.awt.Desktop.getDesktop().browse(
                new java.net.URI("https://wa.me/6281234567890?text=Halo+Fotoimoet,+saya+mau+tanya+tentang+paket")
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
