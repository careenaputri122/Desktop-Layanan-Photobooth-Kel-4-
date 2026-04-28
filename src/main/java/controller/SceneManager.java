package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import dao.UserDAO;
import model.User;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class SceneManager {

    private static Stage stage;
    public static String afterLoginDestination = null; 

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void showHome() throws Exception {
        Parent root = FXMLLoader.load(
            SceneManager.class.getResource("/view/home.fxml")
        );
        Scene scene = new Scene(root, 1280, 800);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/home.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.setMaximized(true);
        stage.centerOnScreen();
    }

    public static void showPricelist() throws Exception {
    Parent root = FXMLLoader.load(
        SceneManager.class.getResource("/view/pricelist.fxml")
    );
    Scene scene = new Scene(root, 1280, 800);
    scene.getStylesheets().add(
        SceneManager.class.getResource("/view/pricelist.css").toExternalForm()
    );
    stage.setScene(scene);
    stage.setMaximized(true);
    stage.centerOnScreen();
}

public static void showPemesanan() throws Exception {
    Parent root = FXMLLoader.load(
        SceneManager.class.getResource("/view/pemesanan.fxml")
    );
    Scene scene = new Scene(root, 1280, 800);
    scene.getStylesheets().add(
        SceneManager.class.getResource("/view/pemesanan.css").toExternalForm()
    );
    stage.setScene(scene);
    stage.setMaximized(true);
    stage.centerOnScreen();
}

    public static void showLogin() throws Exception {
    Parent root = FXMLLoader.load(
        SceneManager.class.getResource("/view/login.fxml")
    );
    Scene scene = new Scene(root, 400, 420);
    scene.getStylesheets().add(
        SceneManager.class.getResource("/view/style.css").toExternalForm()
    );

    Stage popup = new Stage();
    popup.setScene(scene);
    popup.setTitle("Masuk");
    popup.setResizable(false);
    popup.initOwner(stage);                          // terikat ke window utama
    popup.initModality(javafx.stage.Modality.APPLICATION_MODAL); // blok window utama
    popup.centerOnScreen();
    popup.show();
}

public static void closeCurrentPopup() {
    // tutup stage aktif yang bukan stage utama
    javafx.stage.Stage.getWindows().stream()
        .filter(w -> w instanceof javafx.stage.Stage && w != stage && w.isShowing())
        .findFirst()
        .ifPresent(javafx.stage.Window::hide);
}

public static void showRegister() throws Exception {
    Parent root = FXMLLoader.load(
        SceneManager.class.getResource("/view/register.fxml")
    );
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
    
    public static void showGaleri() throws Exception {
    Parent root = FXMLLoader.load(
        SceneManager.class.getResource("/view/galeri.fxml")
    );
    Scene scene = new Scene(root, 1280, 800);
    scene.getStylesheets().add(
        SceneManager.class.getResource("/view/galeri.css").toExternalForm()
    );
    stage.setScene(scene);
    stage.setMaximized(true);
    stage.centerOnScreen();
}
}