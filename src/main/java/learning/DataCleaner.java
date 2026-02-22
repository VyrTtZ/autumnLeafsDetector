package learning;

import javafx.scene.image.*;
import mylinkedlist.MyLinkedList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class DataCleaner {

    static double[][] kernel = {
            {0.0039, 0.0154, 0.0238, 0.0154, 0.0039},
            {0.0154, 0.0599, 0.0949, 0.0599, 0.0154},
            {0.0238, 0.0949, 0.1503, 0.0949, 0.0238},
            {0.0154, 0.0599, 0.0949, 0.0599, 0.0154},
            {0.0039, 0.0154, 0.0238, 0.0154, 0.0039}
    };


    public static void objectSeparator(File f) throws FileNotFoundException {
        Image image = new Image(new FileInputStream(f));
        WritableImage writableImage = new WritableImage(image.getPixelReader(), (int)image.getWidth(), (int)image.getHeight());
        PixelReader reader = image.getPixelReader();
        PixelWriter writer = writableImage.getPixelWriter();
        int width = (int)image.getWidth();



        int height = (int)image.getHeight();
        MyLinkedList mLinkedList = new MyLinkedList();
        int[][] imageVals = new int[height][width];

        int[] pixels = new int[(int)image.getWidth() * (int)image.getHeight()];

        reader.getPixels(0, 0, width, height, WritablePixelFormat.getIntArgbInstance(), pixels, 0, width);


        for(int i = 0; i < pixels.length; i++){
            int r = (i >> 16) & 0xFF;
            int g = (i >> 8)  & 0xFF;
            int b = i & 0xFF;

            pixels[i] = (r + g + b) / 3;
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
                for (int ky = -2; ky <= 2; ky++) {
                    for (int kx = -2; kx <= 2; kx++) {
                        double pixelValue = imageVals[y + ky][x + kx];
                        double weight = kernel[ky + 2][kx + 2];

                        sum += pixelValue * weight;
                    }
                }

                blurred[y][x] = (int)sum;
            }
        }

        for(int i = 0; i < imageVals.length; i++){
            for(int j =0; j<imageVals[i].length; j++){
                System.out.print(blurred[i][j] + " ------");
            }
        }




        
        
        
        
        




    }

}

