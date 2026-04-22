import controller.SceneManager;
import javafx.application.Application;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {

    @Override
    public void start(Stage stage) throws Exception {
        SceneManager.setStage(stage);
        stage.setTitle("Photobooth Management System");
        stage.setResizable(true);  
        SceneManager.showBeranda(); 
        stage.show();
        
    }

    public static void main(String[] args) {
        launch(args);
    }
}
