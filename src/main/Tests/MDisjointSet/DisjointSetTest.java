package MDisjointSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisjointSetTest {
    DisjointSet<mNode<int[]>> testDJSet = new DisjointSet<>();
    mNode<int[]> a, b, c, d, e, f, g, h, i;
    @BeforeEach
    void setUp() {

        a = new mNode<>(new int[]{0, 1});
        b = new mNode<>(new int[]{2, 1});
        c = new mNode<>(new int[]{2, 6});
        d = new mNode<>(new int[]{9, 0});
        e = new mNode<>(new int[]{3, 6});
        f = new mNode<>(new int[]{5, 8});
        g = new mNode<>(new int[]{7, 7});
        h = new mNode<>(new int[]{3, 1});
        i = new mNode<>(new int[]{0, 4});

    }

    @AfterEach
    void tearDown() {
    }


    @Test
    void find() {

    }

    @Test
    void union() {

    }

    @Test
    void orphanage() {

    }
}