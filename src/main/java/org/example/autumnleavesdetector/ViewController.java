package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
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
    private boolean scanOptionFlag = false;
    private boolean colorOptionFlag = false;




    @FXML
    public void initialize() throws FileNotFoundException {
        System.out.println("we r here");
        if(file != null){
            Image image = new Image(new FileInputStream(file));
            WritableImage writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
            PixelReader reader = image.getPixelReader();

            ImageView imgView = new ImageView(writableImage);
            imgView.setFitWidth(imgViewPane.getPrefWidth());
            imgView.setFitHeight(imgViewPane.getPrefHeight());
            imgViewPane.getChildren().clear();
            imgViewPane.getChildren().add(imgView);

            //DataCleaner.objectSeparator(file);

            scanning();

        }

    }

    @FXML private void scanning(){
        scanOption.setOnMousePressed(e ->{
            try {
                openScanOptions();
                scanOptionFlag = true;
            } catch (FileNotFoundException ex) {
                throw new RuntimeException(ex);
            }
        });
    }
    @FXML private void openScanOptions() throws FileNotFoundException {
        if(!scanOptionFlag) {

            optionsBox.setSpacing(3);
            Pane pane = new Pane();
            Pane pane2 = new Pane();

            Canvas canvas = new Canvas();
            canvas.setHeight(pane.getHeight());
            canvas.setWidth(pane.getWidth());

            pane.getChildren().add(canvas);

            pane.setMaxWidth(200);
            pane.setMaxHeight(200);
            pane2.setMaxWidth(200);
            pane2.setMaxHeight(100);

            GraphicsContext graphicsContext = canvasColorFinder.getGraphicsContext2D();
            LinkedList<int[]> temp = new LinkedList<>();

            Image img = new Image(new FileInputStream("src/main/resources/org.example.images/colorWheel.png"));
            mNode<int[]>[] pixelsColorChooser = new mNode[(int)img.getWidth()*(int)img.getHeight()];
            DisjointSet<int[]> ds = new DisjointSet<>();

            for(int i = 0; i < (int) img.getWidth() ; i++){
                for(int j = 0; j < (int)img.getHeight(); j++){
                    pixelsColorChooser[i*(int)img.getWidth()+j] = ds.makeSet(new int[]{i, j});
                }
            }


            PixelReader reader2 = img.getPixelReader();
            graphicsContext.drawImage(img, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            int[] lastX = new int[1];
            int[] lastY = new int[1];


            canvasColorFinder.setOnMousePressed(e -> {
                if (e.getButton() == MouseButton.SECONDARY) { //RIGHT CLICK
                    graphicsContext.clearRect(0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
                    graphicsContext.drawImage(img, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
                    lastX[0] = 0;
                    lastY[0] = 0;
                } else { //LEFT CLICK
                    temp.clear();
                    temp.add(new int[]{(int) e.getX(), (int) e.getY()});
                    lastX[0] = (int) e.getX();
                    lastY[0] = (int) e.getY();

                }
            });

            canvasColorFinder.setOnMouseDragged(e -> {
                double alpha = 0.1;
                double X = alpha * e.getX() + (1 - alpha) * lastX[0];
                double Y = alpha * e.getY() + (1 - alpha) * lastY[0];
                temp.add(new int[]{(int) e.getX(), (int) e.getY()});
                for (int[] i : temp)
                    System.out.println(i[0]);

                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setLineWidth(1);
                graphicsContext.strokeLine(lastX[0], lastY[0], X, Y);
                lastX[0] = (int) e.getX();
                lastY[0] = (int) e.getY();
            });


            canvasColorFinder.setOnMouseReleased(e -> {

                int[] startCoords = temp.getFirst();

                graphicsContext.setStroke(Color.BLACK);
                graphicsContext.setLineWidth(2);
                graphicsContext.strokeLine(e.getX(), e.getY(), startCoords[0], startCoords[1]);

                boolean blag;
                for(int[] t : temp){
                    for(int i = 1; i <img.getWidth(); i++){
                        blag = false;
                        for(int j = 1; j < img.getHeight(); j++){
                            if (t[0] == i && t[1] == j)
                                blag = !blag;
                            if (blag) {
                                ds.union(pixelsColorChooser[(i+1) * (int)img.getWidth() + j], pixelsColorChooser[i*(int)img.getWidth()+j]);


                                System.out.println("oink");
                            }
                            reader2.getArgb(i, j);
                        }
                        }
                    }
                Stage sNew = new Stage();

// Make the canvas the same size as your existing canvas
                Canvas canvasDS = new Canvas(canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
                GraphicsContext gcDS = canvasDS.getGraphicsContext2D();

// Fill background white
                gcDS.setFill(Color.WHITE);
                gcDS.fillRect(0, 0, canvasDS.getWidth(), canvasDS.getHeight());

// Draw pixels in disjoint set as black
                gcDS.setFill(Color.BLACK);
                for (int i = 0; i < img.getWidth(); i++) {
                    for (int j = 0; j < img.getHeight(); j++) {
                        // If pixel belongs to any set (i.e., ds.find(node) is valid), color it black
                        if (ds.find(pixelsColorChooser[i * (int) img.getWidth() + j]) != pixelsColorChooser[i * (int) img.getWidth() + j]) {
                            gcDS.fillRect(i, j, 1, 1);
                        }
                    }
                }

// Put canvas in a pane and show in new stage
                Pane root = new Pane(canvasDS);
                Scene scene = new Scene(root);
                sNew.setScene(scene);
                sNew.setTitle("Disjoint Set Visualization");
                sNew.show();

                });


            pane2.getChildren().add(new Label("Smart Finder"));
            pane2.setOnMousePressed(_ -> {
                searchState = 1;
                pane2.setStyle("-fx-background-color: gray;");
            });

            optionsBox.getChildren().add(pane);
            optionsBox.getChildren().add((pane2));

            MainApp.stage.getScene().addEventHandler(KeyEvent.KEY_RELEASED, t -> {
                if (t.getCode() == KeyCode.ENTER) {
                    System.out.println("searching booohooo");
                }
            });
        }
    }

}
