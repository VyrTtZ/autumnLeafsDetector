package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

public class ImageProcessor {
//-----------------------------------------------------------------------------------------------------------------------
    public static boolean hueInRange(Color c, int[] red, int[] green, int[] blue) { //check if c's paths are within the range of specified paths
        if (c.getSaturation() < 0.1 || c.getBrightness() < 0.1) return false;
        double max = Math.max(c.getRed()*255, Math.max(c.getGreen()*255, c.getBlue()*255));
        double scale = 255.0 / max;
        int rn = (int)(c.getRed()*255   * scale);
        int gn = (int)(c.getGreen()*255 * scale);
        int bn = (int)(c.getBlue()*255  * scale);
        return rn >= red[0] && rn <= red[1] &&
                gn >= green[0] && gn <= green[1] &&
                bn >= blue[0]  && bn <= blue[1];
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static int[] computeColorPaths(PixelReader lassoReader, PixelReader imageReader, int width, int height, double sx, double sy) { //computes the maximum and minimum colors bound by lasso over an image
        int[] r = {255, 0}, g = {255, 0}, b = {255, 0};
        boolean saving = false;
        for (int row = 0; row < height; row++) {
            saving = false;
            for (int col = 0; col < width; col++) {
                Color lasso = lassoReader.getColor(col, row);
                if (lasso.getBrightness() < 0.1) {
                    saving = !saving;
                } else if (saving) {
                    if (lasso.getBrightness() > 0.95) continue; //remove close to white images
                    Color img = imageReader.getColor((int)(col * sx), (int)(row * sy));
                    int ri = (int)(img.getRed()   * 255);
                    int gi = (int)(img.getGreen() * 255);
                    int bi = (int)(img.getBlue()  * 255);
                    if (ri > r[1]) r[1] = ri; if (ri < r[0]) r[0] = ri;
                    if (gi > g[1]) g[1] = gi; if (gi < g[0]) g[0] = gi;
                    if (bi > b[1]) b[1] = bi; if (bi < b[0]) b[0] = bi;
                }
            }
        }
        return new int[]{r[0], r[1], g[0], g[1], b[0], b[1]};
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static mNode<int[]>[] buildDisjointSets(PixelReader reader, int w, int h, int[] red, int[] green, int[] blue, DisjointSet<int[]> ds) { // builds disjoint sets
        mNode<int[]>[] allSpecHueSets = new mNode[w * h];
        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++)
                allSpecHueSets[row * w + col] = ds.makeSet(new int[]{col, row}); //make every pixel its own disjoint set

        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++) {
                Color c = reader.getColor(col, row);
                if (!hueInRange(c, red, green, blue)) continue; //if within the range union it with a root, then check for pixles below and to the right and if they have the same hue then union those
                if (col + 1 < w && hueInRange(reader.getColor(col + 1, row), red, green, blue))
                    ds.union(allSpecHueSets[row * w + col], allSpecHueSets[row * w + col + 1]);
                if (row + 1 < h && hueInRange(reader.getColor(col, row + 1), red, green, blue))
                    ds.union(allSpecHueSets[row * w + col], allSpecHueSets[(row + 1) * w + col]);
            }
        return allSpecHueSets;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static HashMap<mNode<int[]>, Integer> buildMatchedRoots(PixelReader reader, int w, int h, int[] red, int[] green, int[] blue, mNode<int[]>[] djSet, DisjointSet<int[]> ds) {
        HashMap<mNode<int[]>, Integer> matchedRoots = new HashMap<>();
        mNode<int[]> curRoot = new mNode<>(new int[2]);
        int rootcount = 0;
        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++)
                if (hueInRange(reader.getColor(col, row), red, green, blue)){
                    if(curRoot == ds.find(djSet[row * w + col])) rootcount ++;
                    else {
                        matchedRoots.put(curRoot, rootcount);
                        rootcount = 0;
                    }
                    curRoot = ds.find(djSet[row * w + col]);



                }

        return matchedRoots;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static HashMap<mNode<int[]>, int[]> computeBounds(int w, int h, mNode<int[]>[] djSet, DisjointSet<int[]> localDs, HashMap<mNode<int[]>, Integer> matchedRoots) {
        HashMap<mNode<int[]>, int[]> bounds = new HashMap<>();
        for (int row = 0; row < h; row++)
            for (int col = 0; col < w; col++) {
                mNode<int[]> root = localDs.find(djSet[row * w + col]);
                if (!matchedRoots.containsKey(root)) continue;
                if (!bounds.containsKey(root)) bounds.put(root, new int[]{col, row, col, row});
                else {
                    int[] b = bounds.get(root);
                    b[0] = Math.min(b[0], col); b[1] = Math.min(b[1], row);
                    b[2] = Math.max(b[2], col); b[3] = Math.max(b[3], row);
                }
            }
        return bounds;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static LinkedList<int[]> filterBoxes(LinkedList<int[]> boxes, int minSize) {
        LinkedList<int[]> result = new LinkedList<>();
        for (int[] b : boxes)
            if (b[2] - b[0] >= minSize && b[3] - b[1] >= minSize) result.add(b);
        return result;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static int[][] computeCenterPoints(LinkedList<int[]> boxes) {
        int[][] centers = new int[boxes.size()][2];
        for (int i = 0; i < boxes.size(); i++) {
            int[] b = boxes.get(i);
            centers[i] = new int[]{(b[0] + b[2]) / 2, (b[1] + b[3]) / 2};
        }
        return centers;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static int[] findClosest(int curX, int curY, LinkedList<int[]> points) {
        int[] closest = null;
        double closestDist = Double.MAX_VALUE;
        for (int[] point : points) {
            double dist = Math.pow(point[0] - curX, 2) + Math.pow(point[1] - curY, 2);
            if (dist < closestDist) { closestDist = dist; closest = point; }
        }
        return closest;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static LinkedList<int[]> tspOrder(int startX, int startY, int[][] centerPoints) {

        LinkedList<int[]> temp = new LinkedList<>();
        for(int[] i : centerPoints){
            temp.add(i);
        }
        LinkedList<int[]> order = new LinkedList<>();
        int curX = startX, curY = startY;
        while (!temp.isEmpty()) {
            int[] closest = findClosest(curX, curY, temp);
            order.add(closest);
            curX = closest[0]; curY = closest[1];
            temp.remove(closest);
        }
        return order;
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static void drawBresenhamLine(PixelWriter pw, int x0, int y0, int x1, int y1, Color color) {
        int dx = Math.abs(x1 - x0), dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1, sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;
        while (true) {
            pw.setColor(x0, y0, color);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) { err -= dy; x0 += sx; }
            if (e2 <  dx) { err += dx; y0 += sy; }
        }
    }
//-----------------------------------------------------------------------------------------------------------------------
    public static Map<mNode<int[]>, Color> buildSizeGradientMap(HashMap<mNode<int[]>, Boolean> matchedRoots,
                                                                mNode<int[]>[] djSet, DisjointSet<int[]> ds,
                                                                int total) {
        HashMap<mNode<int[]>, Integer> sizes = new HashMap<>();
        for (int i = 0; i < total; i++) {
            mNode<int[]> root = ds.find(djSet[i]);
            if (matchedRoots.containsKey(root)) sizes.merge(root, 1, Integer::sum);
        }
        int min = sizes.values().stream().mapToInt(Integer::intValue).min().orElse(0);
        int max = sizes.values().stream().mapToInt(Integer::intValue).max().orElse(1);
        Map<mNode<int[]>, Color> map = new HashMap<>();
        sizes.forEach((root, size) -> {
            double t = (max == min) ? 0.0 : (double)(size - min) / (max - min);
            map.put(root, Color.color((1-t)*0.1, 0.4+(1-t)*0.6, (1-t)*0.15));
        });
        return map;
    }

}