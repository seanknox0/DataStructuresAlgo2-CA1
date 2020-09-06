package sample;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.lang.reflect.Array;
import java.util.HashSet;

public class Main extends Application {

    public static Stage ps;

    @Override
    public void start(Stage ps1) throws Exception{
        ps = ps1;
        Parent root = FXMLLoader.load(getClass().getResource("A1.fxml"));
        ps.setTitle("Blood Cell Analyser");
        ps.setScene(new Scene(root));
        ps.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
