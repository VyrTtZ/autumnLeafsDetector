package org.example.autumnleavesdetector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static int scene = 0;
    public static Stage stage;
    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        this.stage = stage;
        loadStartView(stage);
    }

    public static void main(String[] args) {
        launch();
    }

    public static void loadStartView(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("start-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 580, 350);
        stage.setTitle("Hello!");
        stage.setScene(scene);
        stage.show();
    }
    public static void loadViewView(Stage stage) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("view-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), 580, 350);
        stage.setTitle("123");
        stage.setScene(scene);
        stage.show();
    }

    public static void setScene(int scene) {
        MainApp.scene = scene;
    }
}