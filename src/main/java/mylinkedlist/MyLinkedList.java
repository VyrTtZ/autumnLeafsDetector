package mylinkedlist;

import java.util.Iterator;
//----------------------------------------------------------------------------------------------------------------------
public class MyLinkedList<T> implements Iterable<T> {
    private Node<T> head; // first node
    public MyLinkedList() {
        head = null;
    }
    //----------------------------------------------------------------------------------------------------------------------
    public void addNode(Node<T> n) {
        if (head == null) {
            head = n;
        } else {
            Node<T> current = head;
            while (current.next != null) current = current.next;
            current.next = n;
        }
    }
    //----------------------------------------------------------------------------------------------------------------------
    public void add(T value) {
        addNode(new Node<>(value, null));
    }
    //----------------------------------------------------------------------------------------------------------------------
    public boolean remove(T item) {
        if (head == null) return false;

        if (head.data.equals(item)) {
            head = head.next;
            return true;
        }

        Node<T> current = head;
        while (current.next != null) {
            if (current.next.data.equals(item)) {
                current.next = current.next.next;
                return true;
            }
            current = current.next;
        }

        return false;
    }
    //----------------------------------------------------------------------------------------------------------------------
    public boolean isEmpty() {
        return head == null;
    }
    //----------------------------------------------------------------------------------------------------------------------
    public int size() {
        int count = 0;
        Node<T> current = head;
        while (current != null) {
            count++;
            current = current.next;
        }
        return count;
    }
    //----------------------------------------------------------------------------------------------------------------------
    public T get(int i) {
        Node<T> current = head;
        int j = 0;

        while (current != null && j < i) {
            current = current.next;
            j++;
        }

        return (current != null) ? current.data : null;
    }
    //----------------------------------------------------------------------------------------------------------------------
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            private Node<T> current = head;

            @Override
            public boolean hasNext() {
                return current != null;
            }

            @Override
            public T next() {
                T value = current.data;
                current = current.next;
                return value;
            }
        };
    }
    //----------------------------------------------------------------------------------------------------------------------
    public boolean contains(T obj) {
            Node node = head;
            while (node != null) {
                if (node.data.equals(obj)) {
                    return true;
                }
                node = node.next;
            }
            return false;

    }
    //----------------------------------------------------------------------------------------------------------------------
}
