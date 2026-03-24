package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import learning.DataCleaner;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;

public class ViewController {
    @FXML
    Pane scanOption, colorModeOption, imgViewPane, pathOption;
    @FXML
    VBox optionsBox;
    @FXML
    Canvas canvasColorFinder;

    public static File file;
    private Image image;
    private WritableImage writableImage;
    private PixelReader reader;
    private ImageView imgView;

    private final DisjointSet<int[]> ds = new DisjointSet<>();
    private mNode<int[]>[] localDJSet;
    private HashMap<mNode<int[]>, Integer> matchedRoots;

    private boolean blackWhiteColorMode = false;
    private boolean randomColorMode = false;
    private boolean gradientColorMode = false;

    private Image imgColorPicker;
    private GraphicsContext gc;
    private int lassoX, lassoY;
    private boolean scanOptionOpen = false;
    private int[][] centerPoints;
//-----------------------------------------------------------------------------------------------------------------------
    @FXML
    public void initialize() throws FileNotFoundException {
        if (file == null) return;
        scanOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);
        colorModeOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);
        pathOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);

        image         = new Image(new FileInputStream(file));
        writableImage = new WritableImage(image.getPixelReader(), w(), h());
        reader        = image.getPixelReader();

        imgViewPane.setPrefSize(MainApp.width * 0.5, MainApp.height * 0.9);
        imgView = new ImageView(writableImage);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(MainApp.width * 0.45);
        imgView.setFitHeight(MainApp.height * 0.85);
        imgView.setX(MainApp.width * 0.025);
        imgView.setY(MainApp.height * 0.025);
        imgViewPane.getChildren().add(imgView);

        DataCleaner.objectSeparator(file);

        scanOption.setOnMousePressed(e      -> openScanOptions());
        colorModeOption.setOnMousePressed(e -> openColorOptions());
        pathOption.setOnMousePressed(e      -> openPathOptions());
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void openScanOptions() {
        if (scanOptionOpen) return;
        scanOptionOpen = true;
        optionsBox.setSpacing(3);
        try { imgColorPicker = new Image(new FileInputStream("src/main/resources/org.example.images/colorWheel.png")); }
        catch (FileNotFoundException ex) { throw new RuntimeException(ex); }
        gc = canvasColorFinder.getGraphicsContext2D();
        gc.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
        canvasColorFinder.setOnMousePressed(this::onLassoPressed);
        canvasColorFinder.setOnMouseDragged(this::onLassoDragged);
        canvasColorFinder.setOnMouseReleased(this::onLassoReleased);
        Pane smartBtn = makePane(200, 100, null, new Label("Smart Finder"));
        smartBtn.setOnMousePressed(_ -> smartBtn.setStyle("-fx-background-color: gray;"));
        optionsBox.getChildren().addAll(new Pane(), smartBtn);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void openColorOptions() {
        if (matchedRoots == null || matchedRoots.isEmpty()) return;
        optionsBox.getChildren().clear();
        optionsBox.setSpacing(3);
        Pane greyscaleOption      = makePane(200, 100, "linear-gradient(to right, black, white)", null);
        Pane randomColorsOption   = makePane(200, 100, "linear-gradient(to right, red, orange, yellow, green, blue, purple)", null);
        Pane greenSizeScaleOption = makePane(200, 100, "linear-gradient(to right, darkgreen, lightgreen)", null);
        greyscaleOption.setOnMouseClicked(_      -> blackAndWhiteRecolor());
        randomColorsOption.setOnMouseClicked(_   -> randomColorRecolor());
        greenSizeScaleOption.setOnMouseClicked(_ -> emeraldGradientRecolor());
        optionsBox.getChildren().addAll(greyscaleOption, randomColorsOption, greenSizeScaleOption);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void openPathOptions() {
        imgViewPane.setOnMouseClicked(e -> TSP((int) e.getX(), (int) e.getY()));
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void onLassoPressed(MouseEvent e) {
        if (e.getButton() == MouseButton.PRIMARY) {
            lassoX = (int) e.getX();
            lassoY = (int) e.getY();
        } else {
            gc.clearRect(0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            gc.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            lassoX = lassoY = 0;
        }
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void onLassoDragged(MouseEvent e) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(lassoX, lassoY, e.getX(), e.getY());
        lassoX = (int) e.getX();
        lassoY = (int) e.getY();
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void onLassoReleased(MouseEvent e) {
        int w = (int) canvasColorFinder.getWidth(), h = (int) canvasColorFinder.getHeight();
        WritableImage snapshot = new WritableImage(w, h);
        canvasColorFinder.snapshot(null, snapshot);

        int[] paths = ImageProcessor.computeColorPaths(
                snapshot.getPixelReader(), imgColorPicker.getPixelReader(), w, h,
                imgColorPicker.getWidth() / w, imgColorPicker.getHeight() / h
        );
        buildComponents(new int[]{paths[0], paths[1]}, new int[]{paths[2], paths[3]}, new int[]{paths[4], paths[5]});
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void buildComponents(int[] red, int[] green, int[] blue) {
        resetImage();
        DisjointSet<int[]> localDs = new DisjointSet<>();
        localDJSet   = ImageProcessor.buildDisjointSets(reader, w(), h(), red, green, blue, localDs);
        matchedRoots = ImageProcessor.buildMatchedRoots(reader, w(), h(), red, green, blue, localDJSet, localDs);
        drawBoundingBoxes(localDs);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void drawBoundingBoxes(DisjointSet<int[]> localDs) {
        resetImage();
        LinkedList<int[]> boxes = ImageProcessor.filterBoxes(
                new LinkedList<>(ImageProcessor.computeBounds(w(), h(), localDJSet, localDs, matchedRoots).values()), 12
        );
        centerPoints = ImageProcessor.computeCenterPoints(boxes);
        for (int[] b : boxes) drawBox(b[0], b[1], b[2], b[3]);
        imgView.setImage(writableImage);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void drawBox(int c0, int r0, int c1, int r1) {
        PixelWriter pw = writableImage.getPixelWriter();
        int djCounter = 0;
        for (mNode<int[]> k : localDJSet) {
            int px = k.getData()[0], py = k.getData()[1];
            if (px >= c0 && px <= c1 && py >= r0 && py <= r1 && ds.find(k) != k) djCounter++;
        }

        for (int c = c0; c <= c1; c++) { pw.setColor(c, r0, Color.BLUE); pw.setColor(c, r1, Color.BLUE); }
        for (int r = r0; r <= r1; r++) { pw.setColor(c0, r, Color.BLUE); pw.setColor(c1, r, Color.BLUE); }
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void recolorPixels(HashMap<mNode<int[]>, Color> colorMap) {
        PixelWriter pw = writableImage.getPixelWriter();
        for (int row = 0; row < h(); row++)
            for (int col = 0; col < w(); col++)
                pw.setColor(col, row, colorMap.getOrDefault(ds.find(localDJSet[idx(row, col)]), Color.BLACK));
        imgView.setImage(writableImage);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void blackAndWhiteRecolor() {
        blackWhiteColorMode = !blackWhiteColorMode;
        randomColorMode = false;
        gradientColorMode = false;

        HashMap<mNode<int[]>, Color> map = new HashMap<>();
        matchedRoots.keySet().forEach(r -> map.put(r, Color.WHITE));
        recolorPixels(map);
        if(blackWhiteColorMode)
            imgViewPane.setOnMouseMoved(e -> {
                Bounds bounds = imgView.localToScene(imgView.getBoundsInLocal());
                int curX = (int)((e.getSceneX() - bounds.getMinX()) * (image.getWidth()  / imgView.getFitWidth()));
                int curY = (int)((e.getSceneY() - bounds.getMinY()) * (image.getHeight() / imgView.getFitHeight()));
                if (curX < 0 || curX >= w() || curY < 0 || curY >= h()) return;
                mNode<int[]> hovered = ds.find(localDJSet[idx(curY, curX)]);
                resetImage();
                blackAndWhiteRecolor();
                PixelWriter pw = writableImage.getPixelWriter();
                for (int row = 0; row < h(); row++)
                    for (int col = 0; col < w(); col++)
                        if (ds.find(localDJSet[idx(row, col)]) == hovered) pw.setColor(col, row, Color.CYAN);
                imgView.setImage(writableImage);
            });
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void randomColorRecolor() {
        randomColorMode = !randomColorMode;
        blackWhiteColorMode = false;
        gradientColorMode = false;
        HashMap<mNode<int[]>, Color> map = new HashMap<>();
        matchedRoots.keySet().forEach(r -> map.put(r, Color.color(Math.random(), Math.random(), Math.random())));
        recolorPixels(map);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void emeraldGradientRecolor() {
        gradientColorMode = !gradientColorMode;
        blackWhiteColorMode = false;
        randomColorMode = false;
        recolorPixels((HashMap<mNode<int[]>, Color>) ImageProcessor.buildSizeGradientMap(matchedRoots, localDJSet, ds, w() * h()));
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void TSP(int x, int y) {
        PixelWriter pw = writableImage.getPixelWriter();
        int curX = x, curY = y;
        for (int[] next : ImageProcessor.tspOrder(x, y, centerPoints)) {
            ImageProcessor.drawBresenhamLine(pw, curX, curY, next[0], next[1], Color.RED);
            curX = next[0]; curY = next[1];
        }
        imgView.setImage(writableImage);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private int w()               { return (int) image.getWidth(); }
    private int h()               { return (int) image.getHeight(); }
    private int idx(int r, int c) { return r * w() + c; }
//-----------------------------------------------------------------------------------------------------------------------
    private void resetImage() {
        writableImage = new WritableImage(reader, w(), h());
        imgView.setImage(writableImage);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private Pane makePane(double w, double h, String gradient, Label label) {
        Pane p = new Pane();
        p.setPrefSize(w, h);
        if (gradient != null) p.setStyle("-fx-background-color: " + gradient + ";");
        if (label    != null) p.getChildren().add(label);
        return p;
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void removeGaps(){
        for(mNode<int[]> n : localDJSet){
            if(n.getData()[0]+1)
        }
    }
}