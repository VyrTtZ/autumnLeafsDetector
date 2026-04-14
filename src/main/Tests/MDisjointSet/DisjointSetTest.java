package MDisjointSet;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DisjointSetTest {
    DisjointSet testDJSet = new DisjointSet<>();
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
        a = b = c = d = e = f = g = h = i = null;
    }

    @Test
    void find() {
        assertSame(a, testDJSet.find(a));
    }

    @Test
    void findSameRoot() {
        testDJSet.union(a, b);
        assertSame(testDJSet.find(a), testDJSet.find(b));
    }

    @Test
    void findNotSame() {
        assertNotSame(testDJSet.find(a), testDJSet.find(b));
    }

    @Test
    void union() {
        testDJSet.union(a, b);
        testDJSet.union(b, c);
        assertSame(testDJSet.find(a), testDJSet.find(c));
    }

    @Test
    void unionSame() {
        testDJSet.union(a, b);
        mNode<int[]> rootBefore = testDJSet.find(a);
        testDJSet.union(a, b);
        assertSame(rootBefore, testDJSet.find(a));
    }

}