package org.example.autumnleavesdetector;

import MDisjointSet.DisjointSet;
import MDisjointSet.mNode;
import org.openjdk.jmh.annotations.*;
import org.openjdk.jmh.infra.Blackhole;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.NANOSECONDS)
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2)
@Measurement(iterations = 5)
public class BenchmarkClass {

    public static void main(String[] args) throws Exception {
        org.openjdk.jmh.Main.main(args);
    }

    DisjointSet<Integer> ds;
    mNode<Integer>[] nodes;
    int SIZE = 10_000;

    @Setup
    public void setup() {
        ds = new DisjointSet<>();
        nodes = new mNode[SIZE];
        for (int i = 0; i < SIZE; i++)
            nodes[i] = ds.makeSet(i);
        for (int i = 0; i < SIZE - 1; i++)
            ds.union(nodes[i], nodes[i + 1]);
    }

    @Benchmark
    public void benchFind(Blackhole bh) {
        for (mNode<Integer> n : nodes)
            bh.consume(ds.find(n));
    }

    @Benchmark
    public void benchUnion(Blackhole bh) {
        for (int i = 0; i < SIZE - 1; i++) {
            ds.union(nodes[i], nodes[i + 1]);
            bh.consume(nodes[i]);
        }
    }
}