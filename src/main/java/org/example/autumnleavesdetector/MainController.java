package org.example.autumnleavesdetector;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;

import java.io.FileInputStream;
import java.io.FileNotFoundException;

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
        System.out.println("test");
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
    }
}