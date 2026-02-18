package org.example.autumnleavesdetector;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.EventListener;

public class MainController {
    @FXML
    private Pane newProjectPane;
    @FXML
    private Pane signOutPane;

    @FXML
    public void initialize() throws FileNotFoundException {
        setPaneBackgrounds();
    }
    @FXML
    protected void setPaneBackgrounds() throws FileNotFoundException {
        ImageView imageAdd = new ImageView(new Image(new FileInputStream("src/main/resources/org.example.images/add.png")));
        ImageView imageQuit = new ImageView(new Image(new FileInputStream("src/main/resources/org.example.images/sign-out.png")));

        imageAdd.setFitWidth(newProjectPane.getPrefWidth());
        imageQuit.setFitWidth(signOutPane.getPrefWidth());

        imageAdd.setFitHeight(newProjectPane.getPrefHeight());
        imageQuit.setFitHeight(newProjectPane.getPrefHeight());

        imageAdd.setPreserveRatio(true);
        imageQuit.setPreserveRatio(true);

        newProjectPane.getChildren().add(imageAdd);
        signOutPane.getChildren().add(imageQuit);

        newProjectPane.setOnMousePressed(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(MainApp.stage);
            if (selectedFile != null) {
                System.out.println("found");
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