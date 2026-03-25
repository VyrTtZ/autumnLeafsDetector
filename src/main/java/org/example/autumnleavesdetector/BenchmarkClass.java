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
@Warmup(iterations = 0)
@Measurement(iterations = 1)

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
    }

    @TearDown
    public void tearDown() {
        ds = null;
        nodes = null;
    }

    @Benchmark
    public void findFlat(Blackhole bh) {
        for (mNode<Integer> n : nodes)
            bh.consume(ds.find(n));
    }

    @Benchmark
    public void findAfterUnions(Blackhole bh) {
        for (int i = 0; i < SIZE - 1; i++)
            ds.union(nodes[i], nodes[i + 1]);
        for (mNode<Integer> n : nodes)
            bh.consume(ds.find(n));
    }

    @Benchmark
    public void unionSequential(Blackhole bh) {
        for (int i = 0; i < SIZE - 1; i++)
            ds.union(nodes[i], nodes[i + 1]);
        bh.consume(nodes);
    }

    @Benchmark
    public void unionRandom(Blackhole bh) {
        for (int i = 0; i < SIZE; i++)
            ds.union(nodes[i], nodes[(int)(Math.random() * SIZE)]);
        bh.consume(nodes);
    }

    @Benchmark
    public void orphanage(Blackhole bh) {
        mNode<Integer> root = ds.find(nodes[0]);
        bh.consume(ds.orphanage(root, nodes));
    }
}