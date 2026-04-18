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
}
