package learning;

public class Weight {
    private Node startNode;
    private Node endNode;
    private double delta;

    public Weight(Node startNode, Node endNode, double delta) {
        this.startNode = startNode;
        this.endNode = endNode;
        this.delta = delta;
    }

    public Node getStartNode() {
        return startNode;
    }

    public void setStartNode(Node startNode) {
        this.startNode = startNode;
    }

    public Node getEndNode() {
        return endNode;
    }

    public void setEndNode(Node endNode) {
        this.endNode = endNode;
    }

    public double getDelta() {
        return delta;
    }

    public void setDelta(double delta) {
        this.delta = delta;
    }
}
