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
        WritableImage writableImage = new WritableImage(image.getPixelReader(), (int)image.getWidth(), (int)image.getHeight());
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = writableImage.getPixelWriter();
        int width = (int)image.getWidth();




        int height = (int)image.getHeight();
        MyLinkedList<int[]> mLinkedList = new MyLinkedList();
        int[][] imageVals = new int[height][width];

        int[] pixels = new int[(int)image.getWidth() * (int)image.getHeight()];

        mNode<int[]>[] pixelsDataImg = new mNode[width * height];
        DisjointSet<int[]> ds = new DisjointSet<>();

        for(int i = 0; i < height; i++){
            for(int j = 0; j < width; j++){
                pixelsDataImg[i* width + j] = ds.makeSet(new int[]{i, j});
            }
        }
        for(mNode i : pixelsDataImg){
            System.out.println("gromp" + " " + i);
        }


        reader.getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);


        for(int i = 0; i < pixels.length; i++){
            int r = (pixels[i] >> 16) & 0xFF;
            int g = (pixels[i] >> 8)  & 0xFF;
            int b =  pixels[i] & 0xFF;
            pixels[i] = (int)(0.299*r + 0.587*g + 0.114*b);
        }


        int pixelCounter = 0;
        for(int i = 0; i < imageVals.length; i++){
            for(int j =0; j<imageVals[i].length; j++){
                imageVals[i][j] = pixels[pixelCounter];
                pixelCounter++;
            }
        }

        double[][] blurred = new double[height][width];

        for (int y = 2; y < height - 2; y++) {
            for (int x = 2; x < width - 2; x++) {
                double sum = 0.0;
                for (int ky = -2; ky <= 2; ky++)
                    for (int kx = -2; kx <= 2; kx++)
                        sum += imageVals[y + ky][x + kx] * kernel[ky+2][kx+2];
                blurred[y][x] = sum;
            }
        }

        System.out.println("done1, blurring is done");
        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {
                int sumHorizontal = 0;
                int sumVertical = 0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        double pixelValue = blurred[y + ky][x + kx];
                        double weightX = sobelKernelX[ky + 1][kx + 1];
                        double weightY = sobelKernelY[ky + 1][kx + 1];

                        sumVertical += pixelValue * weightY;

                        sumHorizontal += pixelValue * weightX;
                    }
                }


                int magnitude = (sumHorizontal*sumHorizontal) + (sumVertical*sumVertical);

                if(magnitude > 150*150) mLinkedList.add(new int[]{y, x});


            }

        }

        boolean[][] isEdge = new boolean[height][width];
        for (int[] i : mLinkedList) isEdge[i[0]][i[1]] = true;

        for (int[] i : mLinkedList) {
            int y = i[0];
            int x = i[1];
            mNode<int[]> current = pixelsDataImg[y * width + x];

            // check 4 directions at distance 4, and fill pixels in between
            if (y >= 4 && isEdge[y-4][x]) {
                for (int step = 1; step <= 4; step++) {
                    isEdge[y-step][x] = true;
                    mLinkedList.add(new int[]{y-step, x});
                    ds.union(current, pixelsDataImg[(y-step) * width + x]);
                }
            }
            else if (y < height - 4 && isEdge[y+4][x]) {
                for (int step = 1; step <= 4; step++) {
                    isEdge[y+step][x] = true;
                    mLinkedList.add(new int[]{y+step, x});
                    ds.union(current, pixelsDataImg[(y+step) * width + x]);
                }
            }
            else if (x >= 4 && isEdge[y][x-4]) {
                for (int step = 1; step <= 4; step++) {
                    isEdge[y][x-step] = true;
                    mLinkedList.add(new int[]{y, x-step});
                    ds.union(current, pixelsDataImg[y * width + (x-step)]);
                }
            }
            else if (x < width - 4 && isEdge[y][x+4]) {
                for (int step = 1; step <= 4; step++) {
                    isEdge[y][x+step] = true;
                    mLinkedList.add(new int[]{y, x+step});
                    ds.union(current, pixelsDataImg[y * width + (x+step)]);
                }
            }
        }


        MyLinkedList<int[]> temp = new MyLinkedList<>();
        for(mNode<int[]> i: pixelsDataImg){
            if(i.getParent() != i){
                temp.add(i.getData());
            }
        }
        temp = filter(mLinkedList, pixelsDataImg, width, ds);

        showEdges(temp, width, height);




        System.out.println("done2, size of edges found raw below: ");
        System.out.println(mLinkedList.size());
        System.out.println("done 3, size of edges found after filling below: ");
        System.out.println(temp.size());
    }
    public static void showEdges(MyLinkedList<int[]> edgePixels, int width, int height) {
        WritableImage edgeImage = new WritableImage(width, height);
        PixelWriter writer = edgeImage.getPixelWriter();

        // Fill background black first
        for (int y = 0; y < height; y++)
            for (int x = 0; x < width; x++)
                writer.setColor(x, y, Color.BLACK);

        // Draw edge pixels white
        for (int i = 0; i < edgePixels.size(); i++) {
            int[] coord = edgePixels.get(i);
            writer.setColor(coord[1], coord[0], Color.WHITE);
        }

        ImageView imageView = new ImageView(edgeImage);
        StackPane root = new StackPane(imageView);
        root.setStyle("-fx-background-color: black;");
        Scene scene = new Scene(root, width, height);

        Stage stage = new Stage();
        stage.setTitle("Edge Detection");
        stage.setScene(scene);
        stage.show();
    }
    private static MyLinkedList<int[]> filter(MyLinkedList<int[]> edgePixels, mNode<int[]>[] pixelsDataImg,int width, DisjointSet<int[]> ds) {
        MyLinkedList<int[]> toRemove = new MyLinkedList<>();

        for (int[] pixel : edgePixels) {
            mNode<int[]> root = ds.find(pixelsDataImg[pixel[0] * width + pixel[1]]);

            // count how many pixels share this root
            int count = 0;
            for (int[] other : edgePixels) {
                if (ds.find(pixelsDataImg[other[0] * width + other[1]]) == root) count++;
            }

            if (count < 10) toRemove.add(pixel);
        }

        for (int[] pixel : toRemove) edgePixels.remove(pixel);

        return edgePixels;
    }

}

