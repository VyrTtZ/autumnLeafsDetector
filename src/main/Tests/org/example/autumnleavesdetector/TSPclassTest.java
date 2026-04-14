package org.example.autumnleavesdetector;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.LinkedList;

import static org.junit.jupiter.api.Assertions.*;

class TSPclassTest {

    int[][] points;

    @BeforeEach
    void setUp() {
        points = new int[][]{{1, 0}, {10, 0}, {20, 0}};
    }

    @AfterEach
    void tearDown() {
        points = null;
    }

    @Test
    void tspOrder() {
        LinkedList<int[]> result = TSPclass.tspOrder(0, 0, points);
        assertEquals(3, result.size());
    }
}