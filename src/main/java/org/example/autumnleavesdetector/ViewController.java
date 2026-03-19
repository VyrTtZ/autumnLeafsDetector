package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Label;
import javafx.scene.image.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import mylinkedlist.MyLinkedList;

import java.io.*;
import java.util.*;

public class ViewController {
    @FXML Pane scanOption, colorModeOption, imgViewPane, pathOption;
    @FXML VBox optionsBox;
    @FXML Canvas canvasColorFinder;

    public static File file;
    private Image image;
    private WritableImage writableImage;
    private PixelReader reader;
    private ImageView imgView;

    private final DisjointSet<int[]> ds = new DisjointSet<>();
    private mNode<int[]>[] localDJSet;
    private HashMap<mNode<int[]>, Boolean> matchedRoots;

    private Image imgColorPicker;
    private GraphicsContext gc;
    private int lassoX, lassoY;
    private final List<int[]> lassoPoints = new LinkedList<>();
    private boolean scanOptionOpen = false;
    private boolean pathOptions = false;
    private int disjointSetIdentificationSize = 0;

    @FXML
    public void initialize() throws FileNotFoundException {
        if (file == null) return;
//---------------------------------------------------------Nav Bar
        scanOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);
        colorModeOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);
        pathOption.setPrefSize((double) MainApp.width/3, (double) MainApp.height/10);
//--------------------------------------------------------Image + image view
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

//-------------------------------------------------------Interaction with navbar
        scanOption.setOnMousePressed(e      -> openScanOptions());
        colorModeOption.setOnMousePressed(e -> openColorOptions());
        pathOption.setOnMousePressed(e -> openPathOptions());
    }
//-------------------------------------------------------------------------------------------------------------------------
    private void openScanOptions() {

        if (scanOptionOpen) return;

        scanOptionOpen = true;
        optionsBox.setSpacing(3);
//----------------------------------------------------------------Setting up the color wheel
        try { imgColorPicker = new Image(new FileInputStream("src/main/resources/org.example.images/colorWheel.png")); }
        catch (FileNotFoundException ex) { throw new RuntimeException(ex); }
//----------------------------------------------------------------Set up for the color wheel lasso
        gc = canvasColorFinder.getGraphicsContext2D();
        gc.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
        canvasColorFinder.setOnMousePressed(this::onLassoPressed);
        canvasColorFinder.setOnMouseDragged(this::onLassoDragged);
        canvasColorFinder.setOnMouseReleased(this::onLassoReleased);
//----------------------------------------------------------------Smart search
        Pane smartBtn = makePane(200, 100, null, new Label("Smart Finder"));
        smartBtn.setOnMousePressed(_ -> smartBtn.setStyle("-fx-background-color: gray;"));
        optionsBox.getChildren().addAll(new Pane(), smartBtn);

    }
//-------------------------------------------------------------------------------------------------------------------------
    private void openColorOptions() {
//---------------------------------------------------------Only allow top open if scan has been done
        if (matchedRoots == null || matchedRoots.isEmpty()) return;
//---------------------------------------------------------Add the panes as buttons for options
        optionsBox.getChildren().clear();
        optionsBox.setSpacing(3);

        Pane greyscaleOption      = makePane(200, 100, "linear-gradient(to right, black, white)", null);
        Pane randomColorsOption  = makePane(200, 100, "linear-gradient(to right, red, orange, yellow, green, blue, purple)", null);
        Pane greenSizeScaleOption = makePane(200, 100, "linear-gradient(to right, darkgreen, lightgreen)", null);
//---------------------------------------------------------Interaction with option panes
        greyscaleOption.setOnMouseClicked(_      -> blackAndWhiteRecolor());
        randomColorsOption.setOnMouseClicked(_  -> randomColorRecolor());
        greenSizeScaleOption.setOnMouseClicked(_ -> emeraldGradientRecolor());

        optionsBox.getChildren().addAll(greyscaleOption, randomColorsOption, greenSizeScaleOption);
    }
//-------------------------------------------------------------------------------------------------------------------------
    private void openPathOptions(){
        if(!pathOptions){
            //starting node for the finding algorithm ts pmo
        }
    }
//-------------------------------------------------------------------------------------------------------------------------
    private void onLassoPressed(MouseEvent e) {
//-----------------------------------------------------Sets up the lasso
        if (e.getButton() == MouseButton.PRIMARY) {
            lassoPoints.clear();
            lassoPoints.add(new int[]{(int) e.getX(), (int) e.getY()});
            lassoX = (int) e.getX();
            lassoY = (int) e.getY();
        } else {
            gc.clearRect(0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            gc.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            lassoX = lassoY = 0;
        }
    }
//-------------------------------------------------------------------------------------------------------------------------
    private void onLassoDragged(MouseEvent e) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(lassoX, lassoY, e.getX(), e.getY());
        lassoX = (int) e.getX();
        lassoY = (int) e.getY();
        lassoPoints.add(new int[]{lassoX, lassoY});
    }
//-------------------------------------------------------------------------------------------------------------------------
private void onLassoReleased(MouseEvent e) {
    int colorFinderWidth  = (int) canvasColorFinder.getWidth();
    int colorFinderHeight = (int) canvasColorFinder.getHeight();

    // Ratios to map canvas coords → image coords
    double sx = imgColorPicker.getWidth()  / colorFinderWidth;
    double sy = imgColorPicker.getHeight() / colorFinderHeight;

    // Read the lasso strokes from the canvas
    WritableImage canvasSnapshot = new WritableImage(colorFinderWidth, colorFinderHeight);
    canvasColorFinder.snapshot(null, canvasSnapshot);
    PixelReader lassoReader = canvasSnapshot.getPixelReader();

    // Read the actual colours from the image underneath
    PixelReader imageReader = imgColorPicker.getPixelReader();

    MyLinkedList<Double> hues = new MyLinkedList<>();
    for (int row = 0; row < colorFinderHeight; row++) {
        boolean saving = false;
        for (int col = 0; col < colorFinderWidth; col++) {
            Color lasso = lassoReader.getColor(col, row);
            if (lasso.getBrightness() < 0.3) {
                saving = !saving; //Toggle the saving of hue after a black pixel
            } else if (saving) {
                Color img = imageReader.getColor((int)(col*sx), (int)(col*sy));
                hues.add(img.getHue());
            }
        }
    }
    int n = hues.size();
    double[] arr = new double[n];
    for (int i = 0; i < n; i++) arr[i] = hues.get(i);


    System.out.println("test1");
mergeSort(arr);

    //Gets the hue range taking the wrap of a color wheel into consideration
    double wrapGap = hues.get(0) + 360 - hues.get(hues.size() - 1);
    int gapIdx = -1;

    for (int i = 0; i < hues.size() - 1; i++)
        if (hues.get(i + 1) - hues.get(i) > wrapGap)
        { wrapGap = hues.get(i + 1) - hues.get(i); gapIdx = i; }

    double minHue = gapIdx == -1 ? hues.get(0)            : hues.get(gapIdx + 1);
    double maxHue = gapIdx == -1 ? hues.get(hues.size()-1) : hues.get(0) + 360;

    System.out.println("min Hue " + minHue);
    System.out.println("max Hue " + maxHue);

        buildComponents(minHue, maxHue);
    }
//-------------------------------------------------------------------------------------------------------------------------

    private void buildComponents(double minHue, double maxHue) {
        resetImage();
        DisjointSet<int[]> localDs = new DisjointSet<>();
        localDJSet = new mNode[w() * h()]; //array to store the disjoint sets


        for (int row = 0; row < h(); row++)
            for (int col = 0; col < w(); col++)
                localDJSet[idx(row, col)] = localDs.makeSet(new int[]{col, row}); //makes every node contain coordinate for every pixel

        for (int row = 0; row < h(); row++) {
            for (int col = 0; col < w(); col++) {
                Color c = reader.getColor(col, row); //Read in every color for each pixle if its within the hue range the union it with pixles that have the same color to the right and below
                if (!hueInRange(c, minHue, maxHue)) continue;
                if (col + 1 < w() && hueInRange(reader.getColor(col + 1, row), minHue, maxHue))
                    localDs.union(localDJSet[idx(row, col)], localDJSet[idx(row, col + 1)]);
                if (row + 1 < h() && hueInRange(reader.getColor(col, row + 1), minHue, maxHue))
                    localDs.union(localDJSet[idx(row, col)], localDJSet[idx(row + 1, col)]);
            }
        }
        System.out.println("test");
        //make a hashmap for roots of the sets of pixles in range
        matchedRoots = new HashMap<>();
        for (int row = 0; row < h(); row++)
            for (int col = 0; col < w(); col++)
                if (hueInRange(reader.getColor(col, row), minHue, maxHue) ){ //for every pixel in the color range add the
                    matchedRoots.put(localDs.find(localDJSet[idx(row, col)]), true); // put found root in the hashmap
                    disjointSetIdentificationSize++;
                }

        drawBoundingBoxes(localDs); //Draw boxes around all the sets
        System.out.println("test2");
    }

    //------------------------------------------------------------------------
    // Bounding Boxes
    //------------------------------------------------------------------------

    private void drawBoundingBoxes(DisjointSet<int[]> localDs) {
        HashMap<mNode<int[]>, int[]> bounds = new HashMap<>();
        for (int row = 0; row < h(); row++) {
            for (int col = 0; col < w(); col++) {
                mNode<int[]> root = localDs.find(localDJSet[idx(row, col)]); //find the root of every disjoint set (pixel)
                if (!matchedRoots.containsKey(root)) continue; //if said root isnt in the root map, nothing done, otherwise compute the bounds
                final int c = col, r = row;
                if (!bounds.containsKey(root)) {
                    bounds.put(root, new int[]{c, r, c, r});
                } else {
                    int[] b = bounds.get(root);
                    b[0] = Math.min(b[0], c); b[1] = Math.min(b[1], r);
                    b[2] = Math.max(b[2], c); b[3] = Math.max(b[3], r);
                }
            }
        }

        resetImage();
        MyLinkedList<int[]> boxes = new MyLinkedList<>();
        for(int[] i : bounds.values()){
            boxes.add(i);
        }
        System.out.println(boxes.size());
        for (int i = boxes.size() - 1; i >= 0; i--) { //checks the size of each box to see if they are larger than 5 px if not, remove
            int[] b = boxes.get(i);
            if (b[2] - b[0] < 5 || b[3] - b[1] < 5)
                boxes.remove(boxes.get(i));
        }
//        boolean merged = true;
//        while (merged) { // runs until there are no more boxes that overlap
//            merged = false;
//            for (int i = 0; i < boxes.size(); i++) { //loop therough evey box and compare it to following boxes
//                for (int j = i + 1; j < boxes.size(); j++) {
//                    if (overlaps(boxes.get(i), boxes.get(j))) {
//                        mergeBox(boxes.get(i), boxes.get(j));
//                        boxes.remove(boxes.get(j));
//                        merged = true;
//                        i = boxes.size(); // force outer loop to exit
//                        break;
//                    }
//                }
//            }
//        }

        for(int[] i : boxes){
            drawBox(i[0], i[1], i[2], i[3]);
        }

        imgView.setImage(writableImage); //update the image on the screen with the boxes
    }

    private void drawBox(int c0, int r0, int c1, int r1) {

        PixelWriter pw = writableImage.getPixelWriter();
        int djCounter = 0;

        for(mNode<int[]> k : localDJSet){
            int px = k.getData()[0];
            int py = k.getData()[1];
            if(px >= c0 && px <= c1 && py >= r0 && py <= r1 && ds.find(k) != k) djCounter++; // count the pixels with the same root within the box
        }

        String str = Integer.toString(djCounter);
        Label l = new Label(str);
        imgViewPane.getChildren().add(l);
        l.toFront();
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #39FF14; -fx-font-weight: bold;");
        double scaleX = imgViewPane.getBoundsInParent().getWidth() / image.getWidth();
        double scaleY = imgViewPane.getBoundsInParent().getHeight() / image.getHeight();
        l.relocate((double)(c0 + c1) / 2 * scaleX, (double)(r0 + r1) / 2 * scaleY); //DISPLAY THE disjoint set size within the box

        for (int c = c0; c <= c1; c++) { pw.setColor(c, r0, Color.NAVY); pw.setColor(c, r1, Color.NAVY); } //draw the columnts of the box
        for (int r = r0; r <= r1; r++) { pw.setColor(c0, r, Color.NAVY); pw.setColor(c1, r, Color.NAVY); } //draw the rows of the box
    }

    private void mergeBox(int[] a, int[] b) { //merges boxes into one
        a[0] = Math.min(a[0], b[0]); a[1] = Math.min(a[1], b[1]);
        a[2] = Math.max(a[2], b[2]); a[3] = Math.max(a[3], b[3]);
    }

    //------------------------------------------------------------------------
    // Recolor
    //------------------------------------------------------------------------

    private void recolorPixels(Map<mNode<int[]>, Color> colorMap) { //main recolor method
        PixelWriter pw = writableImage.getPixelWriter();
        for (int row = 0; row < h(); row++)
            for (int col = 0; col < w(); col++) {
                mNode<int[]> root = ds.find(localDJSet[idx(row, col)]);
                pw.setColor(col, row, colorMap.getOrDefault(root, Color.WHITE));
            }
        imgView.setImage(writableImage);
    }

    private void blackAndWhiteRecolor() {
        Map<mNode<int[]>, Color> map = new HashMap<>();
        matchedRoots.keySet().forEach(r -> map.put(r, Color.BLACK));
        recolorPixels(map);
        imgViewPane.setOnMouseMoved(e -> {
            double scaleX = image.getWidth()  / imgView.getFitWidth();
            double scaleY = image.getHeight() / imgView.getFitHeight();
            int curX = (int) (e.getX() * scaleX);
            int curY = (int) (e.getY() * scaleY);

            if (curX < 0 || curX >= w() || curY < 0 || curY >= h()) return;

            mNode<int[]> hoveredCluster = ds.find(localDJSet[idx(curY, curX)]);

            resetImage();
            PixelWriter pw = writableImage.getPixelWriter();
            for (int row = 0; row < h(); row++) {
                for (int col = 0; col < w(); col++) {
                    if (ds.find(localDJSet[idx(row, col)]) == hoveredCluster)
                        pw.setColor(col, row, Color.CYAN);
                }
            }
            imgView.setImage(writableImage);
        });
    }

    private void randomColorRecolor() {
        Map<mNode<int[]>, Color> map = new HashMap<>();
        matchedRoots.keySet().forEach(r -> map.put(r, Color.color(Math.random(), Math.random(), Math.random())));
        recolorPixels(map);
    }

    private void emeraldGradientRecolor() {
        Map<mNode<int[]>, Integer> sizes = new HashMap<>();
        for (int i = 0; i < w() * h(); i++) {
            mNode<int[]> root = ds.find(localDJSet[i]);
            if (matchedRoots.containsKey(root)) sizes.merge(root, 1, Integer::sum);
        }
        int min = sizes.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = sizes.values().stream().mapToInt(Integer::intValue).max().orElse(1);

        Map<mNode<int[]>, Color> map = new HashMap<>();
        sizes.forEach((root, size) -> {
            double t = (max == min) ? 0.0 : (double)(size - min) / (max - min);
            map.put(root, Color.color((1-t)*0.1, 0.4+(1-t)*0.6, (1-t)*0.15));
        });
        recolorPixels(map);
    }

    private int w()               { return (int) image.getWidth(); }
    private int h()               { return (int) image.getHeight(); }
    private int idx(int r, int c) { return r * w() + c; }

    private void resetImage() {
        writableImage = new WritableImage(reader, w(), h());
        imgView.setImage(writableImage);
    }

    private boolean hueInRange(Color c, double minHue, double maxHue) {
        if (c.getSaturation() < 0.1 || c.getBrightness() < 0.1) return false;
        double hue = c.getHue();
        if (maxHue > 360 && hue < minHue) hue += 360;
        return hue >= minHue && hue <= maxHue;
    }

    private boolean overlaps(int[] a, int[] b) { //checks for the overlapping of the columns and rows whne creating boxes
        return a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1];
    }

    private Pane makePane(double w, double h, String gradient, Label label) { //mapes panes for the navbar
        Pane p = new Pane();
        p.setPrefSize(w, h);
        if (gradient != null) p.setStyle("-fx-background-color: " + gradient + ";");
        if (label    != null) p.getChildren().add(label);
        return p;
    }
    private double[] mergeSort(double[] arr) {
        if (arr.length <= 1) return arr;

        int mid = arr.length / 2;
        double[] left  = new double[mid];
        double[] right = new double[mid];
        for(int i = 0; i < mid; i++) left[i] = arr[i];
        for(int i = mid, r = 0; i < arr.length; i++, r++) right[r] = arr[i];

        return merge(left, right);
    }

    private double[] merge(double[] left, double[] right) {
        double[] result = new double[left.length + right.length];
        int l = 0, r = 0, i = 0;

        while (l < left.length && r < right.length)
            result[i++] = left[l] <= right[r] ? left[l++] : right[r++];

        while (l < left.length)  result[i++] = left[l++];
        while (r < right.length) result[i++] = right[r++]; //check how this works

        return result;
    }
}