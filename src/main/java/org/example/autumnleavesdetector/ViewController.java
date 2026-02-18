package org.example.autumnleavesdetector;

import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;

public class ViewController {

    @FXML
    Pane scanOption, colorModeOption, pathOption, imgViewPane;
    @FXML
    VBox optionsBox;
    public static File file;
    @FXML
    public void initialize() throws FileNotFoundException {
        System.out.println("we r here");
        if(file != null){
            Image image = new Image(new FileInputStream(file));
            WritableImage writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
            ImageView imgView = new ImageView(writableImage);
            imgView.setFitWidth(imgViewPane.getPrefWidth());
            imgView.setFitHeight(imgViewPane.getPrefHeight());
            imgViewPane.getChildren().clear();
            imgViewPane.getChildren().add(imgView);

            scanning();

        }

    }

    @FXML private void scanning(){
        scanOption.setOnMousePressed(e ->{
            try {
                openScanOptions();
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    @FXML private void openScanOptions() throws FileNotFoundException {
        optionsBox.setSpacing(3);
        Pane pane = new Pane();
        Pane pane2 = new Pane();

        Canvas canvas = new Canvas();
        canvas.setHeight(pane.getHeight());
        canvas.setWidth(pane.getWidth());
        canvas.setOnDragDetected(e ->{
            canvas.getGraphicsContext2D().arc(e.getX(), e.getY(), 1, 1, 0, 1);
        });

        pane.getChildren().add(canvas);

        pane.setMaxWidth(200);
        pane.setMaxHeight(200);
        pane2.setMaxWidth(200);
        pane2.setMaxHeight(100);

        ImageView tempImageView = new ImageView(new Image(new FileInputStream("src/main/resources/org.example.images/colorWheel.png")));
        tempImageView.setFitHeight(pane.getMaxHeight());
        tempImageView.setFitWidth(pane.getMaxWidth());
        pane.getChildren().add(tempImageView);
        pane2.getChildren().add(new Label("Smart Finder"));

        optionsBox.getChildren().add(pane);
        optionsBox.getChildren().add((pane2));

        MainApp.stage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, t -> {
            if(t.getCode()== KeyCode.ENTER)
            {
                System.out.println("searching booohooo");
            }
        });
    }

}
