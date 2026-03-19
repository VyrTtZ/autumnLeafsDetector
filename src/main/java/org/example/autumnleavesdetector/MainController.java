package org.example.autumnleavesdetector;

import javafx.fxml.FXML;
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
    private Pane newProjectPane;
    @FXML
    private Pane signOutPane;

    @FXML
    public void initialize() throws FileNotFoundException {
        setPaneBackgrounds();
    }

    private void setPaneBackgrounds() throws FileNotFoundException {
        double paneSize = MainApp.height * 0.25;

        newProjectPane.setPrefSize(paneSize, paneSize);
        signOutPane.setPrefSize(paneSize, paneSize);
        titleHBox.setPrefWidth(MainApp.width);

        // Set up images
        ImageView imageAdd = new ImageView(new Image(new FileInputStream(
                "src/main/resources/org.example.images/add.png")));
        ImageView imageQuit = new ImageView(new Image(new FileInputStream(
                "src/main/resources/org.example.images/sign-out.png")));

        imageAdd.setFitWidth(paneSize);
        imageAdd.setFitHeight(paneSize);
        imageQuit.setFitWidth(paneSize);
        imageQuit.setFitHeight(paneSize);

        imageAdd.setPreserveRatio(true);
        imageQuit.setPreserveRatio(true);

        newProjectPane.getChildren().add(imageAdd);
        signOutPane.getChildren().add(imageQuit);

        newProjectPane.relocate(
                (MainApp.width * 0.25) - (paneSize / 2),
                (MainApp.height * 0.5) - (paneSize / 2)
        );

        signOutPane.relocate(
                (MainApp.width * 0.75) - (paneSize / 2),
                (MainApp.height * 0.5) - (paneSize / 2)
        );

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
    }
}