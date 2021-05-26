package photo_editor;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;

import javafx.stage.Stage;



public class App extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {

        Parent root = FXMLLoader.load(getClass().getResource("UI/image_editor.fxml"));


        Scene scene = new Scene(root);

        primaryStage.setTitle("Image crop");
        primaryStage.setScene(scene);
        primaryStage.show();
    }




}
