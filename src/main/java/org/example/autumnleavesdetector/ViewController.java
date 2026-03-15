package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
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
    private DisjointSet<int[]> ds = new DisjointSet<>();
    private Image image;
    private WritableImage writableImage;
    private PixelReader reader;
    private mNode<int[]>[] imageDJSet;
    private mNode<int[]>[] colorPickerDJSet;

    private int[] lastXlasso = new int[1];
    private int[] lastYlasso = new int[1];

    private ImageView imgView;
    private GraphicsContext graphicsContextColorPicker;
    private LinkedList<int[]> temp = new LinkedList<>();
    private Image imgColorPicker;

    private int colorFinderWidth;
    private int colorFinderHeight;
    private HashMap<mNode<int[]>, Boolean> matchedRoots;
    private mNode<int[]>[] localDJSet;



    //----------------------------------------------------------------------------------------------------------------------------------------
    @FXML
    public void initialize() throws FileNotFoundException {
        if(file != null){
            image = new Image(new FileInputStream(file));
            writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
            reader = image.getPixelReader();

            imgView = new ImageView(writableImage);
            imgView.setFitWidth(imgViewPane.getPrefWidth());
            imgView.setFitHeight(imgViewPane.getPrefHeight());

            imgViewPane.getChildren().clear();
            imgViewPane.getChildren().add(imgView);
//----------------------------------------------------------------------------------------------------------------------------------------
            imageDJSet = new mNode[(int)(image.getWidth() * image.getHeight())];

            for (int i = 0; i < (int) image.getHeight(); i++) {
                for (int j = 0; j < (int) image.getWidth(); j++) {
                    imageDJSet[i * (int)image.getWidth() + j] = ds.makeSet(new int[]{j, i});
                }
            }
//            DataCleaner.objectSeparator(file);

            scanning();
            coloring();

        }

    }
    //----------------------------------------------------------------------------------------------------------------------------------------
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

    @FXML private void coloring(){
        colorModeOption.setOnMousePressed(e ->{
            openColorOptions();
            scanOptionFlag = false;
            colorOptionFlag = true;
        });
    }
    //----------------------------------------------------------------------------------------------------------------------------------------
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
            imgColorPicker = new Image(new FileInputStream("src/main/resources/org.example.images/colorWheel.png"));
            graphicsContextColorPicker = canvasColorFinder.getGraphicsContext2D();
            graphicsContextColorPicker.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            lastXlasso[0] = 0;
            lastYlasso[0] = 0;

            colorFinderWidth = (int) canvasColorFinder.getWidth();
            colorFinderHeight = (int) canvasColorFinder.getHeight();
//----------------------------------------------------------------------------------------------------------------------------------------




            colorPickerDJSet = new mNode[(int)imgColorPicker.getWidth()*(int)imgColorPicker.getHeight()];



            for(int i = 0; i < (int) imgColorPicker.getWidth() ; i++){
                for(int j = 0; j < (int)imgColorPicker.getHeight(); j++){
                    colorPickerDJSet[i*(int)imgColorPicker.getWidth()+j] = ds.makeSet(new int[]{i, j});
                }
            }
            //----------------------------------------------------------------------------------------------------------------------------------------
            canvasColorFinder.setOnMousePressed(e -> {
                mousePressedMethod(e);
            });

            canvasColorFinder.setOnMouseDragged(e -> {
                mouseDraggedMethod(e);
            });


            canvasColorFinder.setOnMouseReleased(e -> {
                mouseReleasedMethod(e);

            });
//----------------------------------------------------------------------------------------------------------------------------------------
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
    //----------------------------------------------------------------------------------------------------------------------------------------
    @FXML private void openColorOptions(){
        if(!matchedRoots.isEmpty()){
            optionsBox.getChildren().clear();
            optionsBox.setSpacing(3);
            Pane pane = new Pane();
            Pane pane2 = new Pane();
            Pane pane3 = new Pane();
            pane.setPrefSize(200, 100);
            pane2.setPrefSize(200, 100);
            pane3.setPrefSize(200, 100);

            pane.setStyle("-fx-background-color: linear-gradient(to right, black, white);");

            pane2.setStyle("-fx-background-color: linear-gradient(to right, red, orange, yellow, green, blue, purple);");

            pane3.setStyle("-fx-background-color: linear-gradient(to right, darkgreen, lightgreen);");
            optionsBox.getChildren().add(pane);
            optionsBox.getChildren().add((pane2));
            optionsBox.getChildren().add(pane3);


            pane.setOnMouseClicked(_ ->{
                System.out.println("test");
                blackAndWhiteRecolor();
            });


        }
    }
    private void mousePressedMethod(MouseEvent e){
        if (e.getButton() == MouseButton.PRIMARY) {
            temp.clear();
            temp.add(new int[]{(int) e.getX(), (int) e.getY()});
            lastXlasso[0] = (int) e.getX();
            lastYlasso[0] = (int) e.getY();
        }
        else {
            graphicsContextColorPicker.clearRect(0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            graphicsContextColorPicker.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            lastXlasso[0] = 0;
            lastYlasso[0] = 0;
        }
    }

    private void mouseDraggedMethod(MouseEvent e){
        graphicsContextColorPicker.setStroke(Color.BLACK);
        graphicsContextColorPicker.setLineWidth(1);
        graphicsContextColorPicker.strokeLine(lastXlasso[0], lastYlasso[0], e.getX(), e.getY());
        lastXlasso[0] = (int) e.getX();
        lastYlasso[0] = (int) e.getY();
        temp.add(new int[]{(int) e.getX(), (int) e.getY()});
        System.out.println(checkIntegrityOfLasso());
    }

    private void mouseReleasedMethod(MouseEvent e) {
        PixelReader colorWheelReader = imgColorPicker.getPixelReader();

        boolean[][] lassoGrid = new boolean[colorFinderHeight][colorFinderWidth];
        for (int[] point : temp) {
            int coordX = point[0];
            int coordY = point[1];
            if (coordX >= 0 && coordX < colorFinderWidth && coordY >= 0 && coordY < colorFinderHeight)
                lassoGrid[coordY][coordX] = true;
        }

        double scaleX = imgColorPicker.getWidth() / canvasColorFinder.getWidth();
        double scaleY = imgColorPicker.getHeight() / canvasColorFinder.getHeight();

        java.util.List<Double> hues = new java.util.ArrayList<>();
        for (int row = 0; row < colorFinderHeight; row++) {
            int firstBlack = -1, lastBlack = -1;
            for (int col = 0; col < colorFinderWidth; col++) {
                if (lassoGrid[row][col]) {
                    if (firstBlack == -1) firstBlack = col;
                    lastBlack = col;
                }
            }
            if (firstBlack == -1) continue;
            for (int col = firstBlack; col <= lastBlack; col++) {
                int imgX = Math.min((int)(col * scaleX), (int)imgColorPicker.getWidth() - 1);
                int imgY = Math.min((int)(row * scaleY), (int)imgColorPicker.getHeight() - 1);
                Color wc = colorWheelReader.getColor(imgX, imgY);
                if (wc.getSaturation() > 0.2)
                    hues.add(wc.getHue());
            }
        }

        if (hues.isEmpty()) return;

        java.util.Collections.sort(hues);
        double biggestGap = 0;
        int gapIdx = 0;
        for (int i = 0; i < hues.size() - 1; i++) {
            double gap = hues.get(i + 1) - hues.get(i);
            if (gap > biggestGap) { biggestGap = gap; gapIdx = i; }
        }
        double wrapGap = (hues.get(0) + 360) - hues.get(hues.size() - 1);
        double minHue, maxHue;
        if (wrapGap > biggestGap) {
            minHue = hues.get(0);
            maxHue = hues.get(hues.size() - 1);
        } else {
            minHue = hues.get(gapIdx + 1);
            maxHue = hues.get(0) + 360;
        }

        System.out.println("hue range: " + minHue + " - " + maxHue);
        setAllColorsManual(minHue, maxHue);
    }

    private void setAllColorsManual(double minHue, double maxHue) {
        resetImage();
        System.out.println("hue range: " + minHue + " - " + maxHue);

        DisjointSet<int[]> localDs = new DisjointSet<>();
        localDJSet = new mNode[(int)(image.getWidth() * image.getHeight())];

        for (int row = 0; row < (int) image.getHeight(); row++) {
            for (int col = 0; col < (int) image.getWidth(); col++) {
                localDJSet[row * (int) image.getWidth() + col] = localDs.makeSet(new int[]{col, row});
            }
        }

        for (int row = 0; row < (int) image.getHeight(); row++) {
            for (int col = 0; col < (int) image.getWidth(); col++) {
                int idx = row * (int) image.getWidth() + col;
                Color c = reader.getColor(col, row);
                if (col + 1 < (int) image.getWidth()) {
                    Color right = reader.getColor(col + 1, row);
                    if (hueInRange(c, minHue, maxHue) && hueInRange(right, minHue, maxHue)) {
                        localDs.union(localDJSet[idx], localDJSet[idx + 1]);
                    }
                }
                if (row + 1 < (int) image.getHeight()) {
                    Color below = reader.getColor(col, row + 1);
                    if (hueInRange(c, minHue, maxHue) && hueInRange(below, minHue, maxHue)) {
                        localDs.union(localDJSet[idx], localDJSet[idx + (int) image.getWidth()]);
                    }
                }
            }
        }

        matchedRoots = new HashMap<>();
        for (int row = 0; row < (int) image.getHeight(); row++) {
            for (int col = 0; col < (int) image.getWidth(); col++) {
                int idx = row * (int) image.getWidth() + col;
                if (hueInRange(reader.getColor(col, row), minHue, maxHue)) {
                    matchedRoots.put(localDs.find(localDJSet[idx]), true);
                }
            }
        }

        HashMap<mNode<int[]>, int[]> rootToBounds = new HashMap<>();

        for (int row = 0; row < (int) image.getHeight(); row++) {
            for (int col = 0; col < (int) image.getWidth(); col++) {
                int idx = row * (int) image.getWidth() + col;
                mNode<int[]> root = localDs.find(localDJSet[idx]);
                if (matchedRoots.containsKey(root)) {
                    if (!rootToBounds.containsKey(root)) {
                        rootToBounds.put(root, new int[]{col, row, col, row});
                    } else {
                        int[] bounds = rootToBounds.get(root);
                        if (col < bounds[0]) bounds[0] = col;
                        if (row < bounds[1]) bounds[1] = row;
                        if (col > bounds[2]) bounds[2] = col;
                        if (row > bounds[3]) bounds[3] = row;
                    }
                }
            }
        }
        for (int[] bounds : rootToBounds.values()) {
            int minCol = bounds[0], minRow = bounds[1], maxCol = bounds[2], maxRow = bounds[3];
            // skip tiny noise regions
            if (maxCol - minCol < 5 || maxRow - minRow < 5) continue;
            // top and bottom edges
            for (int col = minCol; col <= maxCol; col++) {
                writableImage.getPixelWriter().setColor(col, minRow, Color.BLACK);
                writableImage.getPixelWriter().setColor(col, maxRow, Color.BLACK);
            }
            // left and right edges
            for (int row = minRow; row <= maxRow; row++) {
                writableImage.getPixelWriter().setColor(minCol, row, Color.BLACK);
                writableImage.getPixelWriter().setColor(maxCol, row, Color.BLACK);
            }
        }
        resetImage();
        java.util.List<int[]> boxes = new java.util.ArrayList<>(rootToBounds.values());

// filter tiny noise first
        boxes.removeIf(b -> b[2] - b[0] < 5 || b[3] - b[1] < 5);

// keep merging until no more overlaps found
        boolean merged = true;
        while (merged) {
            merged = false;
            for (int i = 0; i < boxes.size(); i++) {
                for (int j = i + 1; j < boxes.size(); j++) {
                    int[] a = boxes.get(i);
                    int[] b = boxes.get(j);
                    if (overlaps(a, b)) {
                        // merge b into a
                        a[0] = Math.min(a[0], b[0]); // minCol
                        a[1] = Math.min(a[1], b[1]); // minRow
                        a[2] = Math.max(a[2], b[2]); // maxCol
                        a[3] = Math.max(a[3], b[3]); // maxRow
                        boxes.remove(j);
                        merged = true;
                        break;
                    }
                }
                if (merged) break;
            }
        }

// draw
        for (int[] bounds : boxes) {
            int minCol = bounds[0], minRow = bounds[1], maxCol = bounds[2], maxRow = bounds[3];
            for (int col = minCol; col <= maxCol; col++) {
                writableImage.getPixelWriter().setColor(col, minRow, Color.BLACK);
                writableImage.getPixelWriter().setColor(col, maxRow, Color.BLACK);
            }
            for (int row = minRow; row <= maxRow; row++) {
                writableImage.getPixelWriter().setColor(minCol, row, Color.BLACK);
                writableImage.getPixelWriter().setColor(maxCol, row, Color.BLACK);
            }
        }



        imgView.setImage(writableImage);

        imgView.setImage(writableImage);
    }

    private boolean overlaps(int[] a, int[] b) {
        // a = {minCol, minRow, maxCol, maxRow}
        return a[0] <= b[2] && a[2] >= b[0] &&
                a[1] <= b[3] && a[3] >= b[1];
    }

    private boolean hueInRange(Color c, double minHue, double maxHue) {
        if (c.getSaturation() < 0.2 || c.getBrightness() < 0.2) return false;
        double hue = c.getHue();
        if (maxHue > 360) hue = hue < minHue ? hue + 360 : hue;
        return hue >= minHue && hue <= maxHue;
    }

    private void resetImage() {
        writableImage = new WritableImage(image.getPixelReader(), (int) image.getWidth(), (int) image.getHeight());
        imgView.setImage(writableImage);
    }

    private boolean checkIntegrityOfLasso(){
        for (int k = 0; k < temp.size() - 1; k++) {
            int[] curr = temp.get(k);
            int[] next = temp.get(k + 1);

            int dx = Math.abs(curr[0] - next[0]);
            int dy = Math.abs(curr[1] - next[1]);
            if (dx > 1 || dy > 1) {
                return false;
            }
        }
        return true;
    }

    private void blackAndWhiteRecolor(){
        for(int i = 0; i < (int)image.getHeight(); i++){
            for(int j = 0; j < (int)image.getWidth(); j++){
                int idx = i * (int)image.getWidth() + j;
                if(matchedRoots.containsKey(ds.find(localDJSet[idx]))){
                    writableImage.getPixelWriter().setColor(j, i, Color.BLACK);
                }
                else writableImage.getPixelWriter().setColor(j, i, Color.WHITE);
            }
        }
        imgView.setImage(writableImage);
    }
}
