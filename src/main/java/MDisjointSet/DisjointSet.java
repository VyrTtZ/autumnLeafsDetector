package MDisjointSet;

public class DisjointSet<T> {
//----------------------------------------Make a node into a disjoint set
    public mNode<T> makeSet(T data) {
        return new mNode<>(data);
    }
//-----------------------------------------Find
    public mNode<T> find(mNode<T> node) {
        if (node != node.getParent()) {
            node.setParent(find(node.getParent()));
        }
        return node.getParent();
    }
//------------------------------------------Union
    public void union(mNode<T> node1, mNode<T> node2) {
        mNode<T> root1 = find(node1);
        mNode<T> root2 = find(node2);

        if (root1 == root2) return;

        root1.setParent(root2);
    }
}