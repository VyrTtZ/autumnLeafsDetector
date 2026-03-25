package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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
    Pane scanOption, colorModeOption, imgViewPane, pathOption;
    @FXML
    VBox optionsBox;
    @FXML
    Canvas canvasColorFinder;
    public static File file;
    private static Image image;
    private WritableImage writableImage;
    private PixelReader reader;
    private ImageView imgView;
    private PixelWriter pw;

    public static final DisjointSet<int[]> ds = new DisjointSet<>();
    private mNode<int[]>[] allPixelsDJsets;
    private HashMap<mNode<int[]>, Integer> roots;
    private HashMap<mNode<int[]>, Integer> rootsWithPlacement;

    private boolean blackWhiteColorMode = false;
    private boolean randomColorMode = false;
    private Image imgColorPicker;
    private GraphicsContext gc;
    private int lassoX, lassoY;
    private boolean scanOptionOpen = false;
    private boolean colorOptionOpen = false;
    private int[][] centerPoints;
//-----------------------------------------------------------------------------------------------------------------------
    @FXML
    public void initialize() throws FileNotFoundException {

//        scanOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10); //Issue because of main.app.witdth = 0;
//        colorModeOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);
//        pathOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);

        image = new Image(new FileInputStream(file));
        writableImage = new WritableImage(image.getPixelReader(), w(), h());
        reader = image.getPixelReader();
        pw = writableImage.getPixelWriter();

        imgViewPane.setPrefSize(600, 600);
        imgView = new ImageView(writableImage);
        imgView.setPreserveRatio(true);
        imgView.setFitWidth(500);
        imgView.setFitHeight(500);
        imgView.setX(100);
        imgView.setY(100);
        imgViewPane.getChildren().add(imgView);


        System.out.println("testiong diddyballz");
        scanOption.setOnMousePressed(e      -> {
            openScanOptions();
            System.out.println("Diddy");
        });
        colorModeOption.setOnMousePressed(e -> openColorOptions());
        pathOption.setOnMousePressed(e      -> openPathOptions());
        hoverMethod();
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void openScanOptions() {
        if (scanOptionOpen) return;

        scanOptionOpen = true;
        colorOptionOpen = false;

        optionsBox.getChildren().clear();
        optionsBox.getChildren().add(canvasColorFinder);
        optionsBox.setSpacing(3);

        imgView.setImage(new WritableImage(reader, w(), h()));

        try {
            imgColorPicker = new Image(new FileInputStream("src/main/resources/org.example.images/colorWheel.png"));
        } catch (FileNotFoundException ex) {
            throw new RuntimeException(ex);
        }

        gc = canvasColorFinder.getGraphicsContext2D();
        gc.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());

        canvasColorFinder.setOnMousePressed(e -> onLassoPressed(e));
        canvasColorFinder.setOnMouseDragged(e -> onLassoDragged(e));
        canvasColorFinder.setOnMouseReleased(e -> onLassoReleased(e));



    }
//-----------------------------------------------------------------------------------------------------------------------
    private void openColorOptions() {
        if (roots == null || roots.isEmpty() || colorOptionOpen) return;

        colorOptionOpen = true;
        scanOptionOpen = false;

        optionsBox.getChildren().clear();
        optionsBox.setSpacing(3);


        Pane greyscaleOption = new Pane();
        greyscaleOption.setPrefSize(200, 200);
        greyscaleOption.setStyle("-fx-background-color: linear-gradient(to right, black, white)");

        Pane randomColorsOption = new Pane();
        randomColorsOption.setPrefSize(200, 200);
        randomColorsOption.setStyle("-fx-background-color: linear-gradient(to right, red, green, blue, purple)");

        Slider sliderGaps = new Slider(0, 144, 8);
        Slider sliderFilter = new Slider(0, 144, 8);



        greyscaleOption.setOnMouseClicked(_      -> blackAndWhiteRecolor());
        randomColorsOption.setOnMouseClicked(_   -> randomColorRecolor());


        optionsBox.getChildren().addAll(greyscaleOption, randomColorsOption, new Label("Gap filling slider"),sliderGaps, new Label("Small filter slider"),sliderFilter);
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
        Label numOfCluster = new Label("Numer of clusters = " + roots.size());
        numOfCluster.setStyle(
                "-fx-font-size: 24px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-text-fill: cyan;" +
                        "-fx-effect: dropshadow(gaussian, cyan, 10, 0.8, 0, 0);" +
                        "-fx-background-color: black;" +
                        "-fx-padding: 8px;" +
                        "-fx-border-color: cyan;" +
                        "-fx-border-width: 2px;"
        );
        optionsBox.getChildren().addAll(new Pane(), numOfCluster);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void buildComponents(int[] red, int[] green, int[] blue) {

        allPixelsDJsets = ImageProcessor.buildDisjointSets(reader, w(), h(), red, green, blue);

        roots = ImageProcessor.buildRoots(reader, w(), h(), red, green, blue, allPixelsDJsets);

        LinkedList<int[]> boxes = ImageProcessor.filterBoxes(
                new LinkedList<>(ImageProcessor.computeBounds(w(), h(), allPixelsDJsets, ds, roots).values()), 12
        );

        centerPoints = ImageProcessor.computeCenterPoints(boxes);

        for (int[] b : boxes)
            drawBox(b[0], b[1], b[2], b[3]);
        imgView.setImage(writableImage);
    }
    private void orderClusters(){
        LinkedList<mNode<int[]>> temp = new LinkedList<>(roots.keySet());
        for (int x = 1; x < temp.size(); x++) { //insertion sort
            mNode<int[]> n = temp.get(x);
            int j = x - 1;
            while (j >= 0 && roots.get(temp.get(j)) > roots.get(n)) {
                temp.set(j + 1, temp.get(j));
                j--;
            }
            temp.set(j + 1, n);
        }
        int index = 1;
        for(mNode<int[]> n : temp){
            rootsWithPlacement.put(n, index);
            index++;
        }
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void drawBox(int c0, int r0, int c1, int r1) {
        for (int c = c0; c <= c1; c++){
            pw.setColor(c, r0, Color.BLUE); pw.setColor(c, r1, Color.BLUE);
        }
        for (int r = r0; r <= r1; r++){
            pw.setColor(c0, r, Color.BLUE); pw.setColor(c1, r, Color.BLUE);
        }
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void recolorPixels(HashMap<mNode<int[]>, Color> colorMap) {
        PixelWriter pw = writableImage.getPixelWriter();
        for (int row = 0; row < h(); row++)
            for (int col = 0; col < w(); col++)
                pw.setColor(col, row, colorMap.getOrDefault(ds.find(allPixelsDJsets[idx(row, col)]), Color.BLACK));
        imgView.setImage(writableImage);
    }
//-----------------------------------------------------------------------------------------------------------------------
    private void blackAndWhiteRecolor() {
        blackWhiteColorMode = !blackWhiteColorMode;
        randomColorMode = false;

        HashMap<mNode<int[]>, Color> map = new HashMap<>();
        for(mNode<int[]> n : roots.keySet())
            map.put(n, Color.WHITE);

        recolorPixels(map);

        if(blackWhiteColorMode)

            imgViewPane.setOnMouseMoved(e -> {

                int curX = (int)e.getX();
                int curY = (int)e.getY();

                if (curX < 0 || curX >= w() || curY < 0 || curY >= h()) return;

                mNode<int[]> hovered = ds.find(allPixelsDJsets[idx(curY, curX)]);

                imgView.setImage(new WritableImage(reader, w(), h()));

                blackAndWhiteRecolor();

                for (int row = 0; row < h(); row++)
                    for (int col = 0; col < w(); col++)
                        if (ds.find(allPixelsDJsets[idx(row, col)]) == hovered) pw.setColor(col, row, Color.RED);

                imgView.setImage(writableImage);

            });
    }

    private void randomColorRecolor() {
        randomColorMode = !randomColorMode;
        blackWhiteColorMode = false;

        HashMap<mNode<int[]>, Color> map = new HashMap<>();
        for(mNode<int[]> n : roots.keySet())
            map.put(n, Color.color(Math.random(), Math.random(), Math.random()));


        recolorPixels(map);
    }

    private void TSP(int x, int y) {
        PixelWriter pw = writableImage.getPixelWriter();
        int curX = x, curY = y;
        for (int[] next : ImageProcessor.tspOrder(x, y, centerPoints)) {
            ImageProcessor.animateTSP(pw, curX, curY, next[0], next[1], Color.RED);
            curX = next[0]; curY = next[1];
        }
        imgView.setImage(writableImage);
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static int w()               { return (int) image.getWidth(); }
    public static  int h()               { return (int) image.getHeight(); }
    private int idx(int r, int c) { return r * w() + c; }
//-----------------------------------------------------------------------------------------------------------------------

    private void hoverMethod(){
        if(!blackWhiteColorMode && !randomColorMode){
            imgViewPane.setOnMouseMoved(e ->{
                if(roots.keySet().contains(ds.find(allPixelsDJsets[idx((int)e.getY(), (int)e.getX())]))){
                    imgViewPane.getChildren().add(new Label(Integer.toString(roots.get(ds.find(allPixelsDJsets[idx((int)e.getY(), (int)e.getX())])))));
                    pw.setColor((int)e.getX(), (int)e.getY(), Color.LIGHTGREEN);
                    System.out.println("testing juicer");
                }
            });
        }
    }

    private void fillandRemoveOutliers(int a, int b){
        int countWithinRadii = 0;
        mNode<int[]> largestRoot = new mNode<>(new int[2]);
        for(mNode<int[]> n : allPixelsDJsets){
            if(roots.get(ds.find(n)) < a) n.setParent(n);


            for(int i = 0; i < b; i++){
                for(int j = 0; j < b; j++){

                    if(ds.find(allPixelsDJsets[idx(n.getData()[0]+ i, n.getData()[1]+j)]) != allPixelsDJsets[idx(n.getData()[0]+ i, n.getData()[1])+j]){
                        if(ds.find(allPixelsDJsets[idx(n.getData()[0]+ i, n.getData()[1]+j)]).getRank() > largestRoot.getRank())
                            largestRoot = ds.find(allPixelsDJsets[idx(n.getData()[0]+ i, n.getData()[1]+j)]);
                        countWithinRadii++;
                    }
                    if(ds.find(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1]-j)]) != allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1])-j]){
                        if(ds.find(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1]-j)]).getRank() > largestRoot.getRank())
                            largestRoot = ds.find(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1]-j)]);
                        countWithinRadii++;
                    }
                    if(ds.find(allPixelsDJsets[idx(n.getData()[0]+i, n.getData()[1]-j)]) != allPixelsDJsets[idx(n.getData()[0]+ i, n.getData()[1])-j]){
                        if(ds.find(allPixelsDJsets[idx(n.getData()[0]+ i, n.getData()[1]-j)]).getRank() > largestRoot.getRank())
                            largestRoot = ds.find(allPixelsDJsets[idx(n.getData()[0]+ i, n.getData()[1]-j)]);
                        countWithinRadii++;
                    }
                    if(ds.find(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1]+j)]) != allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1])+j]){
                        if(ds.find(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1]+j)]).getRank() > largestRoot.getRank())
                            largestRoot = ds.find(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1]+j)]);
                        countWithinRadii++;;
                    }
                }
            }

            if(countWithinRadii > (b / 2)){
                for(int i = 0; i < b; i++){
                    for(int j = 0; j < b; j++){
                        ds.union(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1])-j], largestRoot);
                        ds.union(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1])-j], largestRoot);
                        ds.union(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1])-j], largestRoot);
                        ds.union(allPixelsDJsets[idx(n.getData()[0]- i, n.getData()[1])-j], largestRoot);
                    }
                }
            }
        }
    }
}