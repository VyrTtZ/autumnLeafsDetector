package learning;

public class Node {
    private int layer;
    private static int id;
    private double data;


    public Node(int layer, int id, double data) {
        this.layer = layer;
        this.id = id;
        this.data = data;
    }

    public int getLayer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public int getId() {
        return id;
    }

    public void setId() {
        this.id = id +1;
    }

    public double getData() {
        return data;
    }

    public void setData(double data) {
        this.data = data;
    }
}
