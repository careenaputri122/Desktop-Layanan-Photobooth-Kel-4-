import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class app extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        // Load file FXML dari folder "view"
        Parent root = FXMLLoader.load(getClass().getResource("view/register.fxml"));
        
        Scene scene = new Scene(root);
        
        primaryStage.setTitle("Register");
        primaryStage.setScene(scene);
        primaryStage.show();
    }
    
    public static void main(String[] args) {
        launch(args);
    }
}