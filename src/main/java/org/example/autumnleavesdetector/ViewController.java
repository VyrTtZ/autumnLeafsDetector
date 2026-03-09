package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Rectangle;
import learning.DataCleaner;

import java.awt.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.LinkedList;

public class ViewController {

    @FXML
    Pane scanOption, colorModeOption, pathOption, imgViewPane;
    @FXML
    VBox optionsBox;
    @FXML
    Canvas canvasColorFinder;
    public static File file;
    private static int searchState = 0;

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

            DataCleaner.objectSeparator(file);

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
        canvas.setOnMousePressed(e ->{
            System.out.println("x: " + e.getX());
            canvas.getGraphicsContext2D().beginPath();
            canvas.getGraphicsContext2D().stroke();
        });

        pane.getChildren().add(canvas);

        pane.setMaxWidth(200);
        pane.setMaxHeight(200);
        pane2.setMaxWidth(200);
        pane2.setMaxHeight(100);

        GraphicsContext graphicsContext = canvasColorFinder.getGraphicsContext2D();
        DisjointSet<int[]> lassoPixels = new DisjointSet<>();
        LinkedList<int[]> temp = new LinkedList<>();

        Image img = new Image(new FileInputStream("src/main/resources/org.example.images/colorWheel.png"));
        graphicsContext.drawImage(img, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
        int[] lastX = new int[1];
        int[] lastY = new int[1];


        canvasColorFinder.setOnMousePressed(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                graphicsContext.clearRect(0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
                graphicsContext.drawImage(img , 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
                lastX[0] = 0;
                lastY[0] = 0;
            }
            else{
                temp.clear();
                lastX[0] = (int)e.getX();
                lastY[0] = (int)e.getY();

            }
        });

        canvasColorFinder.setOnMouseDragged(e ->{
            double alpha = 0.1;
            double X = alpha * e.getX() + (1 - alpha) * lastX[0];
            double Y = alpha * e.getY() + (1 - alpha) * lastY[0];
            lassoPixels.makeSet(new int[]{(int)e.getX(), (int)e.getY()});
            temp.add(new int[]{(int)e.getX(), (int)e.getY()});
            for(int[] i : temp)
                System.out.println(i[0]);

            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.setLineWidth(2);
            graphicsContext.strokeLine(lastX[0], lastY[0], X, Y);
            lastX[0] = (int)e.getX();
            lastY[0] = (int)e.getY();
        });



        canvasColorFinder.setOnMouseReleased(e->{
            int[] startCooreds = temp.getFirst();
            graphicsContext.setStroke(Color.BLACK);
            graphicsContext.setLineWidth(2);
            graphicsContext.strokeLine(e.getX(), e.getY(), startCooreds[0], startCooreds[1]);
        });



        pane2.getChildren().add(new Label("Smart Finder"));
        pane2.setOnMousePressed(_ ->{
            searchState = 1;
            pane2.setStyle("-fx-background-color: gray;");
        });

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
