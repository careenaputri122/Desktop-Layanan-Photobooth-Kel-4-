package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;
    public static String afterLoginDestination = null;

    public static void setStage(Stage s) { stage = s; }

    // ── Halaman Publik ────────────────────────────────────────────────────

    public static void showHome() throws Exception {
        loadScene("/view/home.fxml", "/view/home.css", 1280, 800);
    }

    public static void showPricelist() throws Exception {
        loadScene("/view/pricelist.fxml", "/view/pricelist.css", 1280, 800);
    }

    public static void showGaleri() throws Exception {
        loadScene("/view/galeri.fxml", "/view/galeri.css", 1280, 800);
    }

    public static void showPemesanan() throws Exception {
        loadScene("/view/pemesanan.fxml", "/view/pemesanan.css", 1280, 800);
    }

    // ── Halaman Admin ─────────────────────────────────────────────────────

    public static void showAdminDashboard() throws Exception {
        loadScene("/view/admin_dashboard.fxml", "/view/admin_dashboard.css", 1280, 800);
    }

    public static void showKelolaPesanan() throws Exception {
    loadScene("/view/kelola_pesanan.fxml", "/view/kelola_pesanan.css", 1280, 800);
}

    public static void showKelolaPaket() throws Exception {
        loadScene("/view/kelola_paket.fxml", "/view/kelola_paket.css", 1280, 800);
    }
    // ── Popup Login / Register ────────────────────────────────────────────

    public static void showLogin() throws Exception {
        Parent root = FXMLLoader.load(SceneManager.class.getResource("/view/login.fxml"));
        Scene scene = new Scene(root, 400, 420);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/style.css").toExternalForm()
        );
        Stage popup = new Stage();
        popup.setScene(scene);
        popup.setTitle("Masuk");
        popup.setResizable(false);
        popup.initOwner(stage);
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.centerOnScreen();
        popup.show();
    }

    public static void showRegister() throws Exception {
        Parent root = FXMLLoader.load(SceneManager.class.getResource("/view/register.fxml"));
        Scene scene = new Scene(root, 420, 520);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/style.css").toExternalForm()
        );
        Stage popup = new Stage();
        popup.setScene(scene);
        popup.setTitle("Daftar");
        popup.setResizable(false);
        popup.initOwner(stage);
        popup.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        popup.centerOnScreen();
        popup.show();
    }

    public static void closeCurrentPopup() {
        javafx.stage.Stage.getWindows().stream()
            .filter(w -> w instanceof javafx.stage.Stage && w != stage && w.isShowing())
            .findFirst()
            .ifPresent(javafx.stage.Window::hide);
    }

    // ── Helper ────────────────────────────────────────────────────────────

    private static void loadScene(String fxml, String css, double w, double h) throws Exception {
        Parent root = FXMLLoader.load(SceneManager.class.getResource(fxml));
        Scene scene = new Scene(root, w, h);
        scene.getStylesheets().add(SceneManager.class.getResource(css).toExternalForm());
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.centerOnScreen();
    }
}
