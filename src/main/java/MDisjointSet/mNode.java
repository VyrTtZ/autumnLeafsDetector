package MDisjointSet;

import mylinkedlist.MyLinkedList;

public class mNode<T> {
    T data;
    mNode<T> parent;
    int rank;
    //----------------------------------------------------------------------------------------------------------------------
    public mNode(T data) {
        this.data = data;
        this.parent = this;
        this.rank = 0;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public mNode<T> getParent() {
        return parent;
    }

    public void setParent(mNode<T> parent) {
        this.parent = parent;
    }

    public int getRank() {
        return rank;
    }

    public void setRank(int rank) {
        this.rank = rank;
    }

    //----------------------------------------------------------------------------------------------------------------------
}
