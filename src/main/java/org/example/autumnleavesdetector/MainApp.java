package org.example.autumnleavesdetector;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApp extends Application {

    public static int scene = 0;
    public static Stage stage;
    public static int width;
    public static int height;

    @Override
    public void start(Stage stage) throws IOException, InterruptedException {
        this.stage = stage;
        this.stage.setFullScreen(true);

        Rectangle2D screenBounds = Screen.getPrimary().getVisualBounds();

        stage.setX(screenBounds.getMinX());
        stage.setY(screenBounds.getMinY());
        stage.setWidth(screenBounds.getWidth());
        stage.setHeight(screenBounds.getHeight());

        width = (int)screenBounds.getWidth();
        height = (int)screenBounds.getHeight();

        System.out.println("w: " + width + " h: " + height);
        loadStartView(stage);
    }

    public static void main(String[] args) {
        launch();
    }

    public static void loadStartView(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("start-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle("MainView");
        stage.setScene(scene);
        stage.show();
    }
    public static void loadViewView(Stage stage) throws IOException{
        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("view-view.fxml"));

        Scene scene = new Scene(fxmlLoader.load(), width, height);
        stage.setTitle("ImgWorkbench");
        stage.setScene(scene);
        stage.show();
    }

    public static void setScene(int scene) {
        MainApp.scene = scene;
    }
}