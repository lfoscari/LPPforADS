package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.LazyIntIterator;
import it.unimi.dsi.webgraph.NodeIterator;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicIntegerArray;

class ClusterGraphIterator implements Iterator<long[]>, Iterable<long[]> {
    // Try to understand at which speed the clusters go to zero for a given gamma
    final static double DEFAULT_GAMMA = 1./64;
    final static int SEED = 1984;

    public final NodeIterator nodes;
    public final AtomicIntegerArray labels;

    private final IntArrayFIFOQueue arcs = new IntArrayFIFOQueue();

    public ClusterGraphIterator(ImmutableGraph graph) throws IOException {
        LayeredLabelPropagation llp = new LayeredLabelPropagation(graph, SEED);

        this.labels = llp.computeLabels(DEFAULT_GAMMA);
        // If you use computePermutation you take advantage of LLP
        // this.labels = new AtomicIntegerArray(llp.computePermutation(null));
        this.nodes = graph.nodeIterator();
    }

    @Override
    public boolean hasNext() {
        while(true) {
            if (!this.arcs.isEmpty())
                return true;

            if (!this.nodes.hasNext())
                return false;

            int node = this.nodes.nextInt();
            LazyIntIterator successors = this.nodes.successors();
            int label = this.labels.get(node);

            while (true) {
                int successor = successors.nextInt();

                if (successor == -1) break;
                int successor_label = this.labels.get(successor);

                if (label != successor_label) {
                    this.arcs.enqueue(label);
                    this.arcs.enqueue(successor_label);
                }
            }
        }
    }

    @Override
    public long[] next() {
        int source = this.arcs.dequeueInt();
        int destination = this.arcs.dequeueInt();
        return new long[] {source, destination};
    }

    @Override
    public Iterator<long[]> iterator() {
        return this;
    }
}