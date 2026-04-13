package org.example.autumnleavesdetector;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;
import java.util.LinkedList;

public class ImageProcessor {
//-----------------------------------------------------------------------------------------------------------------------
    public static LinkedList<int[]> tspOrder(int startX, int startY, int[][] centerPoints) {
        LinkedList<int[]> temp = new LinkedList<>();
        for(int[] i : centerPoints){
            temp.add(i);
        }

        LinkedList<int[]> order = new LinkedList<>();

        int curX = startX;
        int curY = startY;

        while (!temp.isEmpty()) {
            int[] closest = null;
            double closestDist = Double.MAX_VALUE;
            System.out.println(temp.size());

            for (int[] point : temp) {
                double dist = Math.pow(point[0] - curX, 2) + Math.pow(point[1] - curY, 2);
                if (dist < closestDist) {
                    closestDist = dist;
                    closest = point;
                }
            }

            order.addFirst(closest);

            curX = closest[0];
            curY = closest[1];
            temp.remove(closest);
        }
        return order;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static void animateTSP(PixelWriter pw, int x0, int y0, int x1, int y1, Color color) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx;
        int sy;
        if(x0 < x1)
            sx = 1;
        else sx = -1;
        if(y0 < y1)
            sy = 1;
        else sy = -1;

        int err = dx - dy;
        while (true) {
            pw.setColor(x0, y0, color);
            pw.setColor(x0-1, y0, color);
            pw.setColor(x0+1, y0, color);
            pw.setColor(x0, y0-1, color);
            pw.setColor(x0, y0+1, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 <  dx) { err += dx; y0 += sy; }
        }
    }

}