package learning;

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
            {0.075114, 0.123841, 0.075114},
            {0.123841, 0.204179, 0.123841},
            {0.075114, 0.123841, 0.075114}
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

        reader.getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);


        for(int i = 0; i < pixels.length; i++){
            int r = (i >> 16) & 0xFF;
            int g = (i >> 8)  & 0xFF;
            int b = i & 0xFF;

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

        for (int y = 1; y < height - 1; y++) {
            for (int x = 1; x < width - 1; x++) {

                double sum = 0.0;
                for (int ky = -1; ky <= 1; ky++) {
                    for (int kx = -1; kx <= 1; kx++) {
                        double pixelValue = imageVals[y + ky][x + kx];
                        double weight = kernel[ky + 1][kx + 1];

                        sum += pixelValue * weight;
                    }
                }

                blurred[y][x] = (int)sum;
            }
        }

        System.out.println("done1");
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

                if(magnitude > 50*50) mLinkedList.add(new int[]{y, x});



            }
        }



        System.out.println("done2");
        System.out.println(mLinkedList.size());
    }

}

