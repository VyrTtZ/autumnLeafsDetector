package learning;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.scene.Scene;
import javafx.scene.image.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import mylinkedlist.MyLinkedList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.HashMap;


public class DataCleaner {

    static double[][] kernel = {
            {1/256.0,  4/256.0,  6/256.0,  4/256.0, 1/256.0},
            {4/256.0, 16/256.0, 24/256.0, 16/256.0, 4/256.0},
            {6/256.0, 24/256.0, 36/256.0, 24/256.0, 6/256.0},
            {4/256.0, 16/256.0, 24/256.0, 16/256.0, 4/256.0},
            {1/256.0,  4/256.0,  6/256.0,  4/256.0, 1/256.0}
    };

    static int[][] sobelKernelX ={
            {-1, 0, 1},
            {-2, 0, 2},
            {-1, 0, 1},
    };
    static int[][] sobelKernelY = {
            {1, 2, 1},
            {0, 0, 0},
            {-1, -2, -1}
    };




    public static void objectSeparator(File f) throws FileNotFoundException {
        Image image = new Image(new FileInputStream(f));
        int width = (int)image.getWidth(), height = (int)image.getHeight();
        PixelReader reader = image.getPixelReader();

        // read pixels and convert to greyscale
        int[] pixels = new int[width * height];
        reader.getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);
        int[][] imageVals = new int[height][width];
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++) {
                int p = pixels[i * width + j];
                imageVals[i][j] = (int)(0.299*((p>>16)&0xFF) + 0.587*((p>>8)&0xFF) + 0.114*(p&0xFF));
            }

        // gaussian blur
        double[][] blurred = new double[height][width];
        for (int y = 2; y < height - 2; y++)
            for (int x = 2; x < width - 2; x++) {
                double sum = 0;
                for (int ky = -2; ky <= 2; ky++)
                    for (int kx = -2; kx <= 2; kx++)
                        sum += imageVals[y+ky][x+kx] * kernel[ky+2][kx+2];
                blurred[y][x] = sum;
            }

        // sobel edge detection
        MyLinkedList<int[]> mLinkedList = new MyLinkedList<>();
        boolean[][] isEdge = new boolean[height][width];
        for (int y = 1; y < height - 1; y++)
            for (int x = 1; x < width - 1; x++) {
                int sumH = 0, sumV = 0;
                for (int ky = -1; ky <= 1; ky++)
                    for (int kx = -1; kx <= 1; kx++) {
                        double p = blurred[y+ky][x+kx];
                        sumH += p * sobelKernelX[ky+1][kx+1];
                        sumV += p * sobelKernelY[ky+1][kx+1];
                    }
                if (sumH*sumH + sumV*sumV > 150*150) {
                    mLinkedList.add(new int[]{y, x});
                    isEdge[y][x] = true;
                }
            }

        // build disjoint sets and union adjacent edges
        mNode<int[]>[] pixelsDataImg = new mNode[width * height];
        DisjointSet<int[]> ds = new DisjointSet<>();
        for (int i = 0; i < height; i++)
            for (int j = 0; j < width; j++)
                pixelsDataImg[i * width + j] = ds.makeSet(new int[]{i, j});

        for (int[] p : mLinkedList) {
            mNode<int[]> cur = pixelsDataImg[p[0] * width + p[1]];
            if (p[0] > 0 && isEdge[p[0]-1][p[1]]) ds.union(cur, pixelsDataImg[(p[0]-1) * width + p[1]]);
            if (p[1] > 0 && isEdge[p[0]][p[1]-1]) ds.union(cur, pixelsDataImg[p[0] * width + (p[1]-1)]);
        }


        int gapSize = 6, numDirs = 16;
        for (int[] i : mLinkedList) {
            int y = i[0], x = i[1];
            mNode<int[]> current = pixelsDataImg[y * width + x];
            for (int a = 0; a < numDirs; a++) {
                double angle = 2 * Math.PI * a / numDirs;
                int dy = (int)Math.round(Math.sin(angle)), dx = (int)Math.round(Math.cos(angle));
                if (dy == 0 && dx == 0) continue;
                for (int step = 1; step <= gapSize; step++) {
                    int ny = y + dy*step, nx = x + dx*step;
                    if (ny < 0 || ny >= height || nx < 0 || nx >= width) break;
                    if (isEdge[ny][nx]) {
                        // only fill if the found edge is roughly in the same direction
                        // i.e. don't bridge across large dark regions
                        boolean clearPath = true;
                        for (int check = 1; check < step; check++) {
                            int cy = y + dy*check, cx = x + dx*check;
                            if (imageVals[cy][cx] > 80) { clearPath = false; break; }
                        }
                        if (!clearPath) break;
                        for (int fill = 1; fill < step; fill++) {
                            int fy = y + dy*fill, fx = x + dx*fill;
                            isEdge[fy][fx] = true;
                            mLinkedList.add(new int[]{fy, fx});
                            ds.union(current, pixelsDataImg[fy * width + fx]);
                        }
                        ds.union(current, pixelsDataImg[ny * width + nx]);
                        break;
                    }
                }
            }
        }

        // filter small sets and show
        MyLinkedList<int[]> filtered = filter(mLinkedList, pixelsDataImg, width, ds);
        showEdges(filtered, width, height);
    }
    private static MyLinkedList<int[]> filter(MyLinkedList<int[]> edgePixels, mNode<int[]>[] pixelsDataImg, int width, DisjointSet<int[]> ds) {
        HashMap<mNode<int[]>, Integer> counts = new HashMap<>();
        for (int[] p : edgePixels) {
            mNode<int[]> root = ds.find(pixelsDataImg[p[0] * width + p[1]]);
            counts.put(root, counts.getOrDefault(root, 0) + 1);
        }
        MyLinkedList<int[]> result = new MyLinkedList<>();
        for (int[] p : edgePixels) {
            if (counts.get(ds.find(pixelsDataImg[p[0] * width + p[1]])) >= 5) result.add(p);
        }
        return result;
    }

    public static void showEdges(MyLinkedList<int[]> edgePixels, int width, int height) {
        WritableImage edgeImage = new WritableImage(width, height);
        PixelWriter writer = edgeImage.getPixelWriter();
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                writer.setColor(x, y, Color.BLACK);
        for (int[] p : edgePixels) writer.setColor(p[1], p[0], Color.WHITE);

        Stage stage = new Stage();
        stage.setTitle("Edge Detection");
        stage.setScene(new Scene(new StackPane(new ImageView(edgeImage)), width, height));
        stage.show();
    }
}

