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
import java.io.IOException;

public class MainController {
    @FXML HBox cardRow;
    @FXML HBox header;

    @FXML
    public void initialize() {

        Image imgAdd  = new Image(getClass().getResourceAsStream("/org.example.images/add.png"));
        Image imgQuit = new Image(getClass().getResourceAsStream("/org.example.images/sign-out.png"));

        ImageView imageAdd  = new ImageView(imgAdd);
        ImageView imageQuit = new ImageView(imgQuit);
        imageAdd.setFitWidth(128);  imageAdd.setFitHeight(128);
        imageQuit.setFitWidth(128); imageQuit.setFitHeight(128);
        imageAdd.setPreserveRatio(true);
        imageQuit.setPreserveRatio(true);

        VBox newProjectPane = new VBox(12, imageAdd,  new Label("New project"));
        VBox signOutPane    = new VBox(12, imageQuit, new Label("Sign out"));
        newProjectPane.setAlignment(Pos.CENTER);
        signOutPane.setAlignment(Pos.CENTER);

        newProjectPane.setPrefSize(280, 260);
        signOutPane.setPrefSize(280, 260);

        newProjectPane.setStyle("-fx-background-color: #F1EFE8; -fx-background-radius: 12; -fx-cursor: HAND;");
        signOutPane.setStyle("-fx-background-color: #FCEBEB; -fx-background-radius: 12; -fx-cursor: HAND;");

        cardRow.getChildren().addAll(newProjectPane, signOutPane);

        newProjectPane.setOnMousePressed(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
            File selectedFile = fileChooser.showOpenDialog(MainApp.stage);
            if (selectedFile != null) {
                ViewController.file = selectedFile;
                try { MainApp.loadViewView(MainApp.stage); }
                catch (IOException ex) { throw new RuntimeException(ex); }
            }
        });
        signOutPane.setOnMousePressed(e -> Platform.exit());
    }
}