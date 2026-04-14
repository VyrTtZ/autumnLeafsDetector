package MDisjointSet;

public class mNode<T> {
    T data;
    mNode<T> parent;
    //----------------------------------------------------------------------------------------------------------------------
    public mNode(T data) {
        this.data = data;
        this.parent = this;
    }

    public T getData() {
        return data;
    }

    public mNode<T> getParent() {
        return parent;
    }

    public void setParent(mNode<T> parent) {
        this.parent = parent;
    }}

    //----------------------------------------------------------------------------------------------------------------------

