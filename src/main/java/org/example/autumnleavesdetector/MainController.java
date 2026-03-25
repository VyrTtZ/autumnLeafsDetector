package org.example.autumnleavesdetector;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class MainController {
    @FXML
    private HBox titleHBox;

    @FXML
    public void initialize() throws FileNotFoundException {
        setPaneBackgrounds();
    }

    private void setPaneBackgrounds() throws FileNotFoundException {
        double paneSize = MainApp.height * 0.25;

        Pane newProjectPane = new Pane();
        Pane signOutPane = new Pane();

        newProjectPane.setPrefSize(paneSize, paneSize);
        signOutPane.setPrefSize(paneSize, paneSize);
        titleHBox.setPrefWidth(MainApp.width);
        titleHBox.setSpacing(600);
        titleHBox.setAlignment(Pos.CENTER_LEFT);

        ImageView imageAdd = new ImageView(new Image(new FileInputStream("src/main/resources/org.example.images/add.png")));
        ImageView imageQuit = new ImageView(new Image(new FileInputStream("src/main/resources/org.example.images/sign-out.png")));

        imageAdd.setFitWidth(paneSize);
        imageAdd.setFitHeight(paneSize);
        imageQuit.setFitWidth(paneSize);
        imageQuit.setFitHeight(paneSize);

        imageAdd.setPreserveRatio(true);
        imageQuit.setPreserveRatio(true);

        newProjectPane.getChildren().add(imageAdd);
        signOutPane.getChildren().add(imageQuit);
        titleHBox.getChildren().add(newProjectPane);
        titleHBox.getChildren().add(signOutPane);


        newProjectPane.setOnMousePressed(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(MainApp.stage);
            if (selectedFile != null) {
                ViewController.file = selectedFile;
                try {
                    MainApp.loadViewView(MainApp.stage);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        signOutPane.setOnMousePressed(e ->
                        Platform.exit()
                );
    }
}