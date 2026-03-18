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

import java.io.*;
import java.util.*;

public class ViewController {

    //------------------------------------------------------------------------
    // FXML
    //------------------------------------------------------------------------

    @FXML Pane scanOption, colorModeOption, imgViewPane;
    @FXML VBox optionsBox;
    @FXML Canvas canvasColorFinder;

    //------------------------------------------------------------------------
    // Image
    //------------------------------------------------------------------------

    public static File file;
    private Image image;
    private WritableImage writableImage;
    private PixelReader reader;
    private ImageView imgView;

    //------------------------------------------------------------------------
    // Disjoint Set
    //------------------------------------------------------------------------

    private final DisjointSet<int[]> ds = new DisjointSet<>();
    private mNode<int[]>[] localDJSet;
    private HashMap<mNode<int[]>, Boolean> matchedRoots;

    //------------------------------------------------------------------------
    // Color Picker Lasso
    //------------------------------------------------------------------------

    private Image imgColorPicker;
    private GraphicsContext gc;
    private int lassoX, lassoY;
    private final List<int[]> lassoPoints = new LinkedList<>();
    private boolean scanOptionOpen = false;
    private int disjointSetIdentificationSize = 0;

    //------------------------------------------------------------------------
    // Init
    //------------------------------------------------------------------------

    @FXML
    public void initialize() throws FileNotFoundException {
        if (file == null) return;
        System.out.println((double) MainApp.width/3);
        scanOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);
        scanOption.setStyle("linear-gradient(to right, black, white)");
        colorModeOption.setPrefSize((double) MainApp.width / 3, (double) MainApp.height / 10);
        scanOption.getChildren().add(new Label("testing"));

        image         = new Image(new FileInputStream(file));
        writableImage = new WritableImage(image.getPixelReader(), w(), h());
        reader        = image.getPixelReader();

        imgView = new ImageView(writableImage);
        imgView.setFitWidth(MainApp.width/2);
        imgView.setFitHeight(MainApp.height);
        imgViewPane.getChildren().setAll(imgView);

        scanOption.setOnMousePressed(e      -> openScanOptions());
        colorModeOption.setOnMousePressed(e -> openColorOptions());
    }

    //------------------------------------------------------------------------
    // Scan Panel
    //------------------------------------------------------------------------

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

        MainApp.stage.getScene().addEventHandler(
                javafx.scene.input.KeyEvent.KEY_RELEASED,
                t -> { if (t.getCode() == KeyCode.ENTER) System.out.println("searching"); }
        );
    }

    //------------------------------------------------------------------------
    // Color Panel
    //------------------------------------------------------------------------

    private void openColorOptions() {
        if (matchedRoots == null || matchedRoots.isEmpty()) return;
        optionsBox.getChildren().clear();
        optionsBox.setSpacing(3);

        Pane bw      = makePane(200, 100, "linear-gradient(to right, black, white)", null);
        Pane random  = makePane(200, 100, "linear-gradient(to right, red, orange, yellow, green, blue, purple)", null);
        Pane emerald = makePane(200, 100, "linear-gradient(to right, darkgreen, lightgreen)", null);

        bw.setOnMouseClicked(_      -> blackAndWhiteRecolor());
        random.setOnMouseClicked(_  -> randomColorRecolor());
        emerald.setOnMouseClicked(_ -> emeraldGradientRecolor());

        optionsBox.getChildren().addAll(bw, random, emerald);
    }

    //------------------------------------------------------------------------
    // Lasso Events
    //------------------------------------------------------------------------

    private void onLassoPressed(MouseEvent e) {
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

    private void onLassoDragged(MouseEvent e) {
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(lassoX, lassoY, e.getX(), e.getY());
        lassoX = (int) e.getX();
        lassoY = (int) e.getY();
        lassoPoints.add(new int[]{lassoX, lassoY});
    }

    private void onLassoReleased(MouseEvent e) {
        int cw = (int) canvasColorFinder.getWidth(), ch = (int) canvasColorFinder.getHeight();
        boolean[][] grid = new boolean[ch][cw];
        for (int[] p : lassoPoints)
            if (p[0] >= 0 && p[0] < cw && p[1] >= 0 && p[1] < ch)
                grid[p[1]][p[0]] = true;

        double sx = imgColorPicker.getWidth() / canvasColorFinder.getWidth();
        double sy = imgColorPicker.getHeight() / canvasColorFinder.getHeight();
        PixelReader pr = imgColorPicker.getPixelReader();

        List<Double> hues = new ArrayList<>();
        for (int row = 0; row < ch; row++) {
            int first = -1, last = -1;
            for (int col = 0; col < cw; col++)
                if (grid[row][col]) { if (first == -1) first = col; last = col; }
            if (first == -1) continue;
            for (int col = first; col <= last; col++) {
                Color c = pr.getColor(Math.min((int)(col * sx), (int) imgColorPicker.getWidth() - 1),
                        Math.min((int)(row * sy), (int) imgColorPicker.getHeight() - 1));
                if (c.getSaturation() > 0.2) hues.add(c.getHue());
            }
        }
        if (hues.isEmpty()) return;

        Collections.sort(hues);
        double bigGap = 0; int gapIdx = 0;
        for (int i = 0; i < hues.size() - 1; i++) {
            double g = hues.get(i + 1) - hues.get(i);
            if (g > bigGap) { bigGap = g; gapIdx = i; }
        }
        double wrapGap = hues.get(0) + 360 - hues.get(hues.size() - 1);
        double minHue  = wrapGap > bigGap ? hues.get(0)             : hues.get(gapIdx + 1);
        double maxHue  = wrapGap > bigGap ? hues.get(hues.size()-1) : hues.get(0) + 360;

        buildComponents(minHue, maxHue);
    }

    //------------------------------------------------------------------------
    // Component Detection
    //------------------------------------------------------------------------

    private void buildComponents(double minHue, double maxHue) {
        resetImage();
        DisjointSet<int[]> localDs = new DisjointSet<>();
        localDJSet = new mNode[w() * h()];

        for (int row = 0; row < h(); row++)
            for (int col = 0; col < w(); col++)
                localDJSet[idx(row, col)] = localDs.makeSet(new int[]{col, row});

        for (int row = 0; row < h(); row++) {
            for (int col = 0; col < w(); col++) {
                Color c = reader.getColor(col, row);
                if (!hueInRange(c, minHue, maxHue)) continue;
                if (col + 1 < w() && hueInRange(reader.getColor(col + 1, row), minHue, maxHue))
                    localDs.union(localDJSet[idx(row, col)], localDJSet[idx(row, col + 1)]);
                if (row + 1 < h() && hueInRange(reader.getColor(col, row + 1), minHue, maxHue))
                    localDs.union(localDJSet[idx(row, col)], localDJSet[idx(row + 1, col)]);
            }
        }

        matchedRoots = new HashMap<>();
        for (int row = 0; row < h(); row++)
            for (int col = 0; col < w(); col++)
                if (hueInRange(reader.getColor(col, row), minHue, maxHue) ){
                    matchedRoots.put(localDs.find(localDJSet[idx(row, col)]), true);
                    disjointSetIdentificationSize++;
                    System.out.println(" ------ : " + disjointSetIdentificationSize);
                }

        drawBoundingBoxes(localDs);
    }

    //------------------------------------------------------------------------
    // Bounding Boxes
    //------------------------------------------------------------------------

    private void drawBoundingBoxes(DisjointSet<int[]> localDs) {
        Map<mNode<int[]>, int[]> bounds = new HashMap<>();
        for (int row = 0; row < h(); row++) {
            for (int col = 0; col < w(); col++) {
                mNode<int[]> root = localDs.find(localDJSet[idx(row, col)]);
                if (!matchedRoots.containsKey(root)) continue;
                final int c = col, r = row;
                bounds.compute(root, (k, b) -> {
                    if (b == null) return new int[]{c, r, c, r};
                    b[0] = Math.min(b[0], c); b[1] = Math.min(b[1], r);
                    b[2] = Math.max(b[2], c); b[3] = Math.max(b[3], r);
                    return b;
                });
            }
        }

        resetImage();
        List<int[]> boxes = new ArrayList<>(bounds.values());
        boxes.removeIf(b -> b[2] - b[0] < 5 || b[3] - b[1] < 5);

        boolean merged = true;
        while (merged) {
            merged = false;
            outer:
            for (int i = 0; i < boxes.size(); i++)
                for (int j = i + 1; j < boxes.size(); j++)
                    if (overlaps(boxes.get(i), boxes.get(j))) {
                        mergeBox(boxes.get(i), boxes.get(j));
                        boxes.remove(j);
                        merged = true; break outer;
                    }
        }

        boxes.forEach(b -> drawBox(b[0], b[1], b[2], b[3]));
        imgView.setImage(writableImage);
    }

    private void drawBox(int c0, int r0, int c1, int r1) {
        PixelWriter pw = writableImage.getPixelWriter();
        int size = c1-c0;
        int size2 = r1-r0;
        int djCounter = 0;
        for(mNode<int[]> k : localDJSet){
            int px = k.getData()[0];
            int py = k.getData()[1];
            if(px >= c0 && px <= c1 && py >= r0 && py <= r1 && ds.find(k) != k) djCounter++;
        }

        System.out.println("size --  : " + size + " blehh " + size2);
        String str = Integer.toString(djCounter);
        Label l = new Label(str);
        imgViewPane.getChildren().add(l);
        l.toFront();
        l.setStyle("-fx-font-size: 12px; -fx-text-fill: #39FF14; -fx-font-weight: bold;");
        double scaleX = imgViewPane.getBoundsInParent().getWidth() / image.getWidth();
        double scaleY = imgViewPane.getBoundsInParent().getHeight() / image.getHeight();

        l.relocate((double)(c0 + c1) / 2 * scaleX, (double)(r0 + r1) / 2 * scaleY);
        for (int c = c0; c <= c1; c++) { pw.setColor(c, r0, Color.BLACK); pw.setColor(c, r1, Color.BLACK); }
        for (int r = r0; r <= r1; r++) { pw.setColor(c0, r, Color.BLACK); pw.setColor(c1, r, Color.BLACK); }
    }

    private void mergeBox(int[] a, int[] b) {
        a[0] = Math.min(a[0], b[0]); a[1] = Math.min(a[1], b[1]);
        a[2] = Math.max(a[2], b[2]); a[3] = Math.max(a[3], b[3]);
    }

    //------------------------------------------------------------------------
    // Recolor
    //------------------------------------------------------------------------

    private void recolorPixels(Map<mNode<int[]>, Color> colorMap) {
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
//        imgViewPane.setOnMouseMoved(e -> { DOESNT WORK FOR NOW, HIGHLIGHT THE CLUSTER THAT MOUSE HOVERS OVER
//            int curX = (int) e.getX();
//            int curY = (int) e.getY();
//            System.out.println("x: " + curX + " y: " + curY);
//            // find which cluster the hovered pixel belongs to
//            mNode<int[]> hoveredCluster = ds.find(localDJSet[idx(curY, curX)]); // or however you get the node at a pixel
//
//            for(mNode<int[]> node : matchedRoots.keySet()){
//                if(ds.find(node) == hoveredCluster){
//                    System.out.println("GRAPEEEE");
//                    writableImage.getPixelWriter().setColor(node.getData()[0], node.getData()[1], Color.CYAN);
//                }
//            }
//        });
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

    //------------------------------------------------------------------------
    // Utilities
    //------------------------------------------------------------------------

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

    private boolean overlaps(int[] a, int[] b) {
        return a[0] <= b[2] && a[2] >= b[0] && a[1] <= b[3] && a[3] >= b[1];
    }

    private Pane makePane(double w, double h, String gradient, Label label) {
        Pane p = new Pane();
        p.setPrefSize(w, h);
        if (gradient != null) p.setStyle("-fx-background-color: " + gradient + ";");
        if (label    != null) p.getChildren().add(label);
        return p;
    }
}