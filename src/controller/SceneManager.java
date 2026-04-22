package controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SceneManager {

    private static Stage stage;

    public static void setStage(Stage s) {
        stage = s;
    }

    public static void showLogin() throws Exception {
        Parent root = FXMLLoader.load(
            SceneManager.class.getResource("/view/login.fxml")
        );
        Scene scene = new Scene(root, 480, 560);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/style.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void showRegister() throws Exception {
        Parent root = FXMLLoader.load(
            SceneManager.class.getResource("/view/register.fxml")
        );
        Scene scene = new Scene(root, 480, 650);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/style.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.centerOnScreen();
    }

    public static void showBeranda() throws Exception {
        Parent root = FXMLLoader.load(
            SceneManager.class.getResource("/view/beranda.fxml")
        );
        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/beranda.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.setMaximized(true); 
        stage.centerOnScreen();
    }

    public static void showPricelist() throws Exception {
        Parent root = FXMLLoader.load(
            SceneManager.class.getResource("/view/pricelist.fxml")
        );
        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/style.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.centerOnScreen();
    }
 
    public static void showGaleri() throws Exception {
        Parent root = FXMLLoader.load(
            SceneManager.class.getResource("/view/galeri.fxml")
        );
        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/style.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.centerOnScreen();
    }
 
    public static void showPemesanan() throws Exception {
        Parent root = FXMLLoader.load(
            SceneManager.class.getResource("/view/pemesanan.fxml")
        );
        Scene scene = new Scene(root, 1200, 720);
        scene.getStylesheets().add(
            SceneManager.class.getResource("/view/style.css").toExternalForm()
        );
        stage.setScene(scene);
        stage.centerOnScreen();
    }
}

