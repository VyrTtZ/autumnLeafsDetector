package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.image.*;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.util.Duration;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.LinkedList;

public class ViewController {

    @FXML Pane imgViewPane;
    @FXML StackPane scanOption, colorModeOption, pathOption;
    @FXML VBox optionsBox;
    @FXML Canvas canvasColorFinder;

    public static File file;
    private static Image image;
    private WritableImage writableImage;
    private PixelReader reader;
    private static ImageView imgView;
    private PixelWriter pw;
    private int h, w;

    private static final DisjointSet<int[]> ds = new DisjointSet<>();
    private mNode<int[]>[] allPixelsDJsets;
    private HashMap<mNode<int[]>, Integer> roots = new HashMap<>();
    private HashMap<mNode<int[]>, Integer> rootsWithPlacement = new HashMap<>();
    private HashMap<mNode<int[]>, int[]> bounds = new HashMap<>();
    private LinkedList<int[]> boxes = new LinkedList<>();
    private int[][] centerPoints;

    private Image imgColorPicker;
    private GraphicsContext gc;
    private int lassoX, lassoY;
    private double hueMin = 360, hueMax = 0;
    private double smoothX, smoothY;

    private boolean scanOptionOpen = false;
    private boolean colorOptionOpen = false;
    private boolean blackWhiteColorMode = false;
    private boolean randomColorMode = false;
    private int gapInt = 0, terminateInt = 0;
    private Canvas imgData;
    private Label numOfCluster;

    private final ContextMenu hoverMenu = new ContextMenu();
    private final MenuItem hoverItem = new MenuItem();

    //------------------------------------------------------------------------------------------------------------------ INIT

    @FXML
    public void initialize() throws FileNotFoundException {
        try {
            image = new Image(new FileInputStream(file), (int) imgViewPane.getWidth(), (int) imgViewPane.getHeight(), true, true);
        } catch (FileNotFoundException e) { throw new RuntimeException(e); }

        w = (int) image.getWidth();
        h = (int) image.getHeight();

        writableImage = new WritableImage(image.getPixelReader(), w, h);
        reader = writableImage.getPixelReader();
        pw = writableImage.getPixelWriter();

        imgView = new ImageView(writableImage);
        imgView.setPreserveRatio(true);
        imgView.fitWidthProperty().bind(imgViewPane.widthProperty());
        imgView.fitHeightProperty().bind(imgViewPane.heightProperty());

        imgViewPane.getChildren().add(imgView);

        imgData = new Canvas();
        imgData.widthProperty().bind(imgViewPane.widthProperty());
        imgData.heightProperty().bind(imgViewPane.heightProperty());


        imgViewPane.getChildren().add(imgData);

        scanOption.setOnMousePressed(_      -> openScanOptions());
        colorModeOption.setOnMousePressed(_ -> openColorOptions());
        pathOption.setOnMousePressed(_      -> openPathOptions());

        Platform.runLater(() -> scanOption.getScene().getRoot().layout());
    }

    //------------------------------------------------------------------------------------------------------------------ PROJECT MANAGEMENT OPTIONS

    private void openScanOptions() {
        if (scanOptionOpen) return; //if already open

        scanOptionOpen  = true;
        colorOptionOpen = false;

        allPixelsDJsets = new mNode[w * h]; //reset everything
        roots.clear();
        rootsWithPlacement.clear();
        bounds.clear();
        boxes.clear();
        centerPoints = null;
        writableImage = new WritableImage(image.getPixelReader(), w, h);
        pw = writableImage.getPixelWriter();
        imgView.setImage(writableImage);

        imgColorPicker = new Image(getClass().getResourceAsStream("/org.example.images/colorWheel.png"));
        gc = canvasColorFinder.getGraphicsContext2D();
        gc.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());

        canvasColorFinder.setOnMousePressed(e -> onLassoPressed(e));
        canvasColorFinder.setOnMouseDragged(e -> onLassoDragged(e));
        canvasColorFinder.setOnMouseReleased(_ -> onLassoReleased());
        imgViewPane.setOnMouseMoved(e -> hoverDetails(e));

        optionsBox.getChildren().setAll(canvasColorFinder);
    }

    private void openColorOptions() {
        if (roots == null || roots.isEmpty() || colorOptionOpen) return;
        colorOptionOpen = true;
        scanOptionOpen  = false;

        imgData.getGraphicsContext2D().clearRect(0, 0, imgData.getWidth(), imgData.getHeight());

        Pane greyscaleOption = new Pane();
        greyscaleOption.setPrefSize(300, 60);
        greyscaleOption.setStyle("-fx-background-color: linear-gradient(to right, black, white); -fx-background-radius: 6;");

        Pane randomColorsOption = new Pane();
        randomColorsOption.setPrefSize(300, 60);
        randomColorsOption.setStyle("-fx-background-color: linear-gradient(to right, red, green, blue, purple); -fx-background-radius: 6;");


        Slider sliderGaps   = new Slider(0, 70, 0);
        Slider sliderFilter = new Slider(0, 70, 0);


        greyscaleOption.setOnMouseClicked(_    -> blackAndWhiteRecolor());
        randomColorsOption.setOnMouseClicked(_ -> randomColorRecolor());


        sliderGaps.setOnMouseReleased(_ -> {
            gapInt = (int) sliderGaps.getValue();
            buildSets();
            fill(gapInt);
            resetAndRedraw();
        });

        sliderFilter.setOnMouseReleased(_ -> {
            terminateInt = (int) sliderFilter.getValue();
            buildSets();
            smallOnesRemoval(terminateInt);
            resetAndRedraw();
        });


        optionsBox.getChildren().setAll(
                greyscaleOption, randomColorsOption,
                new Label("Gap filling slider"),  sliderGaps,
                new Label("Small filter slider"), sliderFilter
        );
        optionsBox.setSpacing(10);
    }

    private void openPathOptions() {
        imgViewPane.setOnMouseClicked(e -> {

            if(!blackWhiteColorMode) {
                int currX = (int) (e.getX() * w / imgView.getBoundsInLocal().getWidth());
                int currY = (int) (e.getY() * h / imgView.getBoundsInLocal().getHeight());

                TSP(currX, currY);
            }else{
//                Alert a = new Alert(Alert.AlertType.INFORMATION);
//                DialogPane dp = new DialogPane();
//                dp.contentProperty().set(new Label("This is not usable in black/white mode"));
//                a.setDialogPane(dp);
//                a.show();
//
//                openScanOptions();
//                blackWhiteColorMode = !blackWhiteColorMode;
            }
        });
    }

    //------------------------------------------------------------------------------------------------------------------ LASSO METHODS

    private void onLassoPressed(MouseEvent e) {
        if(optionsBox.getChildren().contains(numOfCluster))
            optionsBox.getChildren().remove(numOfCluster);

        hueMin = 360; hueMax = 0;
        if (e.getButton() == MouseButton.PRIMARY) {
            lassoX = (int) e.getX(); lassoY = (int) e.getY();
            smoothX = lassoX; smoothY = lassoY;
        } else {
            gc.clearRect(0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            gc.drawImage(imgColorPicker, 0, 0, canvasColorFinder.getWidth(), canvasColorFinder.getHeight());
            resetAndRedraw();
            lassoX = lassoY = 0;
        }
    }

    private void onLassoDragged(MouseEvent e) {
        if (!e.isPrimaryButtonDown()) return;
        smoothX += (e.getX() - smoothX) * 0.1;
        smoothY += (e.getY() - smoothY) * 0.1;

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeLine(lassoX, lassoY, smoothX, smoothY);
        lassoX = (int) smoothX;
        lassoY = (int) smoothY;

        Color c = imgColorPicker.getPixelReader().getColor(lassoX, lassoY);
        if (c.getSaturation() > 0.01 && c.getBrightness() > 0.01) {
            double hue = c.getHue();
            if (hue > hueMax) hueMax = hue;
            if (hue < hueMin) hueMin = hue;
        }
    }

    private void onLassoReleased() {
        buildSets();
        setupRoots();
        smallOnesRemoval(20);
        orderClusters();
        setupBounds();
        manageBoxes();
        drawBoxes();
        drawNumbers();

        imgView.setImage(writableImage);
        numOfCluster = new Label("Estimating : " + roots.size() + " leaves / clusters");
        optionsBox.getChildren().add(numOfCluster);
    }

    private void hoverDetails(MouseEvent e) {
        int currX = (int)(e.getX() * w / imgView.getBoundsInLocal().getWidth());
        int currY = (int)(e.getY() * h / imgView.getBoundsInLocal().getHeight());
        mNode<int[]> root = null;
        if (!inBounds(currY, currX)) { hoverMenu.hide(); return; }
        if(allPixelsDJsets[idx(currY, currX)] != null)
            root = ds.find(allPixelsDJsets[idx(currY, currX)]);
        if(root != null) {
            if (!roots.containsKey(root)) {
                hoverMenu.hide();
                return;
            }

            int size = roots.get(root);
            int number = rootsWithPlacement.getOrDefault(root, -1);

            hoverItem.setText("leaf " +  number + " — " + size + " px");
            hoverMenu.getItems().setAll(hoverItem);
            hoverMenu.show(imgData, e.getScreenX() + 10, e.getScreenY() + 10);
        }
    }

    //------------------------------------------------------------------------------------------------------------------ BUILD OF SETS

    private void buildSets() {
        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++)
                allPixelsDJsets[idx(row, col)] = ds.makeSet(new int[]{col, row});

        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++) {
                Color c = reader.getColor(col, row);
                if (!hueInRange(c)) continue;
                if (col + 1 < w && hueInRange(reader.getColor(col + 1, row))){
                    ds.union(allPixelsDJsets[idx(row, col)], allPixelsDJsets[idx(row, col) + 1]);
                }

                if (row + 1 < h && hueInRange(reader.getColor(col, row + 1))) {
                    ds.union(allPixelsDJsets[idx(row, col)], allPixelsDJsets[(row + 1) * w + col]);
                }
            }
    }

    private void setupRoots() {
        roots = new HashMap<>();
        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++)
                if (hueInRange(reader.getColor(col, row))) {
                    mNode<int[]> root = ds.find(allPixelsDJsets[idx(row, col)]);
                    if (roots.containsKey(root)) roots.put(root, roots.get(root) + 1);
                    else roots.put(root, 1);
                }
    }

    private void setupBounds() {

        bounds = new HashMap<>();

        for (int i = 0; i < h; i++)
            for (int j = 0; j < w; j++) {

                mNode<int[]> root = ds.find(allPixelsDJsets[idx(i, j)]);
                if (!roots.containsKey(root)) continue;

                if (!bounds.containsKey(root))
                    bounds.put(root, new int[]{j, i, j, i}); //x1, y1, x2, y2
                else {
                    int[] b = bounds.get(root);
                    if (j < b[0]) b[0] = j;
                    if (i < b[1]) b[1] = i;
                    if (j > b[2]) b[2] = j;
                    if (i > b[3]) b[3] = i;
                }
            }
    }

    private void manageBoxes() {

        boxes.clear();

        for (int[] b : bounds.values())
            boxes.add(b); //adds the 2 corners

        centerPoints = new int[roots.size()][2];
        for (int i = 0; i < boxes.size(); i++) {
            int[] b = boxes.get(i);
            centerPoints[i] = new int[]{(b[0] + b[2]) / 2, (b[1] + b[3]) / 2}; //finds center points
        }
    }

    private void orderClusters() { //sets up roots with placement
        LinkedList<mNode<int[]>> temp = new LinkedList<>(roots.keySet());
        for (int x = 1; x < temp.size(); x++) {
            mNode<int[]> n = temp.get(x);
            int j = x - 1;
            while (j >= 0 && roots.get(temp.get(j)) < roots.get(n)) {
                temp.set(j + 1, temp.get(j)); j--;
            }
            temp.set(j + 1, n);
        }
        int index = 1;
        for (mNode<int[]> n : temp) rootsWithPlacement.put(n, index++);
    }

    //------------------------------------------------------------------------------------------------------------------ RESULT MANAGEMENT

    private void fill(int dist) {
        for (mNode<int[]> n : roots.keySet()) {
            int coordX = n.getData()[0];
            int coordY = n.getData()[1];

            for (int i = 1; i < 361; i++) {
                double angle = (2 * Math.PI * i) / 360.0;
                boolean exited = false;
                int exitJ = -1;

                for (int j = 1; j < dist; j++) {
                    int dx = (int)(coordX + j * Math.cos(angle));
                    int dy = (int)(coordY + j * Math.sin(angle));
                    if (!inBounds(dx, dy)) break;

                    mNode<int[]> p = allPixelsDJsets[idx(dy, dx)];
                    if (ds.find(p) != n) {
                        exited = true;
                        exitJ = j;
                        break;
                    }
                }

                if (!exited) continue;

                for (int j = exitJ; j < dist; j++) {
                    int dx = (int)(coordX + j * Math.cos(angle));
                    int dy = (int)(coordY + j * Math.sin(angle));
                    if (!inBounds(dx, dy)) break;

                    mNode<int[]> p = allPixelsDJsets[idx(dy, dx)];
                    boolean notRoot = ds.find(p) != p;

                    if (notRoot) {
                        for (int k = exitJ; k < j; k++) {
                            int dx2 = (int)(coordX + k * Math.cos(angle));
                            int dy2 = (int)(coordY + k * Math.sin(angle));
                            ds.union(allPixelsDJsets[idx(dy2, dx2)], n);
                        }
                        break;
                    }
                }
            }
        }
    }

    private void smallOnesRemoval(int b) {
        LinkedList<mNode<int[]>> toRemove = new LinkedList<>();
        for (mNode<int[]> n : roots.keySet())
            if (roots.get(n) < b) toRemove.add(n);
        for (mNode<int[]> n : toRemove) roots.remove(n);

        for (mNode<int[]> m : allPixelsDJsets)
            if (!roots.containsKey(ds.find(m)))
                ds.union(m, m);
    }

    //------------------------------------------------------------------------------------------------------------------ DRAWING METHODS

    private void drawBoxes() {
        for (int[] b : boxes) {
            for (int c = b[0]; c <= b[2]; c++) {
                pw.setColor(c, b[1], Color.BLUE);
                pw.setColor(c, b[3], Color.BLUE);
            }
            for (int r = b[1]; r <= b[3]; r++) {
                pw.setColor(b[0], r, Color.BLUE);
                pw.setColor(b[2], r, Color.BLUE);
            }
        }
    }
    private void drawNumbers() {
        GraphicsContext gc = imgData.getGraphicsContext2D();
        gc.clearRect(0, 0, imgData.getWidth(), imgData.getHeight());

        gc.setFont(Font.font(9));
        gc.setFill(Color.GREEN);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);


        for (mNode<int[]> n : rootsWithPlacement.keySet()) {
            int[] b = bounds.get(n);
            if (b == null) continue;

            double x = ((b[0] + b[2]) / 2.0) * (imgView.getBoundsInLocal().getWidth()  / w);
            double y = ((b[1] + b[3]) / 2.0) * (imgView.getBoundsInLocal().getHeight() / h);

            String text = Integer.toString(rootsWithPlacement.get(n));
            gc.strokeText(text, x, y);
            gc.fillText(text,   x, y);
        }
    }

    private void resetAndRedraw() {
        roots.clear();
        rootsWithPlacement.clear();
        bounds.clear();
        setupRoots();
        System.out.println(roots.size());
        smallOnesRemoval(terminateInt == 0 ? 20 : terminateInt);
        orderClusters();
        setupBounds();
        manageBoxes();

        writableImage = new WritableImage(image.getPixelReader(), w, h);
        pw = writableImage.getPixelWriter();
        drawBoxes();
        imgView.setImage(writableImage);
    }

    private void blackAndWhiteRecolor() {
        blackWhiteColorMode = !blackWhiteColorMode;
        randomColorMode = false;

        HashMap<mNode<int[]>, Color> map = new HashMap<>();
        for (mNode<int[]> n : roots.keySet()) map.put(n, Color.WHITE);

        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++)
                pw.setColor(col, row, map.getOrDefault(ds.find(allPixelsDJsets[idx(row, col)]), Color.BLACK));

        imgView.setImage(writableImage);


        if(blackWhiteColorMode)

            imgViewPane.setOnMouseMoved(e -> {

                int curX = (int)(e.getX() * w / imgView.getBoundsInLocal().getWidth());
                int curY = (int)(e.getY() * h / imgView.getBoundsInLocal().getHeight());

                if (curX < 0 || curX >= w || curY < 0 || curY >= h) return;

                mNode<int[]> hovered = ds.find(allPixelsDJsets[idx(curY, curX)]);

                imgView.setImage(new WritableImage(reader, w, h));

                blackAndWhiteRecolor();

                for (int row = 0; row < h; row++)
                    for (int col = 0; col < w; col++)
                        if (ds.find(allPixelsDJsets[idx(row, col)]) == hovered) pw.setColor(col, row, Color.RED);

                imgView.setImage(writableImage);

            });
    }

    private void randomColorRecolor() {
        randomColorMode = !randomColorMode;
        blackWhiteColorMode = false;

        HashMap<mNode<int[]>, Color> map = new HashMap<>();
        for (mNode<int[]> n : roots.keySet())
            map.put(n, Color.color(Math.random(), Math.random(), Math.random()));

        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++)
                pw.setColor(col, row, map.getOrDefault(ds.find(allPixelsDJsets[idx(row, col)]), Color.BLACK));

        imgView.setImage(writableImage);
    }

    private void TSP(int x, int y) {
        LinkedList<int[]> order = TSPclass.tspOrder(closestCenter(x,y)[0], closestCenter(x,y)[1], centerPoints);
        Timeline tl = new Timeline();
        int curX = x, curY = y;

        for (int i = 0; i < order.size(); i++) {
            int fx = curX, fy = curY;
            int tx = order.get(i)[0], ty = order.get(i)[1];
            tl.getKeyFrames().add(new KeyFrame(Duration.millis(i * 3000.0 / order.size()), e -> {
                TSPclass.animateTSP(pw, fx, fy, tx, ty, Color.RED);
                imgView.setImage(writableImage);
            }));
            curX = tx; curY = ty;
        }
        tl.play();
    }

    //------------------------------------------------------------------------------------------------------------------ UTIL METHODS

    private boolean hueInRange(Color c) {
        if (c.getSaturation() < 0.01 || c.getBrightness() < 0.01) return false;
        return c.getHue() >= hueMin && c.getHue() <= hueMax;
    }

    private int idx(int r, int c) { return r * w + c; }

    private boolean inBounds(int r, int c) { return r >= 0 && r < h && c >= 0 && c < w; }

    private int[] closestCenter(int x, int y) {
        int[] closest = null;
        double dist = Double.MAX_VALUE;

        for (int[] center : centerPoints) {
            if (center == null) continue;
            double d = Math.pow(center[0] - x, 2) + Math.pow(center[1] - y, 2);
            if (d < dist) {
                dist = d;
                closest = center;
            }
        }
        return closest;
    }

    //------------------------------------------------------------------------------------------------------------------ THE END

}