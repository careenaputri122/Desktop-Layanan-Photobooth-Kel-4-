package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class BerandaController {

    @FXML
    private void handlePesan() {
        try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleLihatPaket() {
        try { SceneManager.showPricelist(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePricelist() {
        try { SceneManager.showPricelist(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleGaleri() {
        try { SceneManager.showGaleri(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handlePemesanan() {
        try { SceneManager.showPemesanan(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAdmin() {
        try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleMember() {
        try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleMasuk() {
        try { SceneManager.showLogin(); } catch (Exception e) { e.printStackTrace(); }
    }

     @FXML
    private void handleRegister() {
        try { SceneManager.showRegister(); } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleAmbilPromo1() {
        showPromoAlert("Promo Lebaran Spesial",
                "Diskon 20% untuk semua paket cetak selama bulan April.\nCocok untuk gathering keluarga dan acara Lebaran!");
    }

    @FXML
    private void handleAmbilPromo2() {
        showPromoAlert("Paket Wisuda Hemat",
                "Spesial wisuda semester ini!\nDapatkan tambahan 50 lembar cetak gratis untuk setiap pemesanan.");
    }

    @FXML
    private void handleAmbilPromo3() {
        showPromoAlert("Wedding Package Premium",
                "Paket pernikahan lengkap dengan backdrop mewah,\noperator berpengalaman, dan album foto eksklusif.");
    }

    private void showPromoAlert(String title, String content) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Promo");
        alert.setHeaderText(title);
        alert.setContentText(content + "\n\nSilakan login atau hubungi kami untuk memesan!");
        alert.showAndWait();
    }
}