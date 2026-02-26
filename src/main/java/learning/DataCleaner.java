package learning;

import javafx.scene.image.*;
import mylinkedlist.MyLinkedList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;


public class DataCleaner {

    static double[][] kernel = {
            {0.000254, 0.000431, 0.000676, 0.000976, 0.001298, 0.001592, 0.001800, 0.001875, 0.001800, 0.001592, 0.001298, 0.000976, 0.000676, 0.000431, 0.000254},
            {0.000431, 0.000733, 0.001149, 0.001659, 0.002207, 0.002707, 0.003059, 0.003187, 0.003059, 0.002707, 0.002207, 0.001659, 0.001149, 0.000733, 0.000431},
            {0.000676, 0.001149, 0.001800, 0.002599, 0.003458, 0.004241, 0.004793, 0.004993, 0.004793, 0.004241, 0.003458, 0.002599, 0.001800, 0.001149, 0.000676},
            {0.000976, 0.001659, 0.002599, 0.003752, 0.004993, 0.006123, 0.006921, 0.007209, 0.006921, 0.006123, 0.004993, 0.003752, 0.002599, 0.001659, 0.000976},
            {0.001298, 0.002207, 0.003458, 0.004993, 0.006644, 0.008149, 0.009210, 0.009594, 0.009210, 0.008149, 0.006644, 0.004993, 0.003458, 0.002207, 0.001298},
            {0.001592, 0.002707, 0.004241, 0.006123, 0.008149, 0.009993, 0.011295, 0.011766, 0.011295, 0.009993, 0.008149, 0.006123, 0.004241, 0.002707, 0.001592},
            {0.001800, 0.003059, 0.004793, 0.006921, 0.009210, 0.011295, 0.012766, 0.013298, 0.012766, 0.011295, 0.009210, 0.006921, 0.004793, 0.003059, 0.001800},
            {0.001875, 0.003187, 0.004993, 0.007209, 0.009594, 0.011766, 0.013298, 0.013852, 0.013298, 0.011766, 0.009594, 0.007209, 0.004993, 0.003187, 0.001875},
            {0.001800, 0.003059, 0.004793, 0.006921, 0.009210, 0.011295, 0.012766, 0.013298, 0.012766, 0.011295, 0.009210, 0.006921, 0.004793, 0.003059, 0.001800},
            {0.001592, 0.002707, 0.004241, 0.006123, 0.008149, 0.009993, 0.011295, 0.011766, 0.011295, 0.009993, 0.008149, 0.006123, 0.004241, 0.002707, 0.001592},
            {0.001298, 0.002207, 0.003458, 0.004993, 0.006644, 0.008149, 0.009210, 0.009594, 0.009210, 0.008149, 0.006644, 0.004993, 0.003458, 0.002207, 0.001298},
            {0.000976, 0.001659, 0.002599, 0.003752, 0.004993, 0.006123, 0.006921, 0.007209, 0.006921, 0.006123, 0.004993, 0.003752, 0.002599, 0.001659, 0.000976},
            {0.000676, 0.001149, 0.001800, 0.002599, 0.003458, 0.004241, 0.004793, 0.004993, 0.004793, 0.004241, 0.003458, 0.002599, 0.001800, 0.001149, 0.000676},
            {0.000431, 0.000733, 0.001149, 0.001659, 0.002207, 0.002707, 0.003059, 0.003187, 0.003059, 0.002707, 0.002207, 0.001659, 0.001149, 0.000733, 0.000431},
            {0.000254, 0.000431, 0.000676, 0.000976, 0.001298, 0.001592, 0.001800, 0.001875, 0.001800, 0.001592, 0.001298, 0.000976, 0.000676, 0.000431, 0.000254}
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

        for (int y = 7; y < height - 7; y++) {
            for (int x = 7; x < width - 7; x++) {

                double sum = 0.0;
                for (int ky = -7; ky <= 7; ky++) {
                    for (int kx = -7; kx <= 7; kx++) {
                        double pixelValue = imageVals[y + ky][x + kx];
                        double weight = kernel[ky + 7][kx + 7];

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

                if(magnitude > 35*35) mLinkedList.add(new int[]{y, x});
            }
        }

        for(int i = 0; i < mLinkedList.size(); i++){
                for(int k = 5; k <10; k++){
                    int[] temp = mLinkedList.get(i);
                    for(int j =0; j < mLinkedList.size(); j++){
                        int[] temp2 = mLinkedList.get(i);
                        if(temp[0]+k == temp2[0]) System.out.println("Sean");
                        if(temp[1]+k == temp2[1]) System.out.println("Jeffrey");
                        if(temp[0]-k == temp2[0]) System.out.println("Diddy");
                        if(temp[1]-k == temp2[1]) System.out.println("Epstein");
                }
            }
        }

        System.out.println("done2");

        //System.out.println(mLinkedList.size());




        
        
        
        
        




    }

}

