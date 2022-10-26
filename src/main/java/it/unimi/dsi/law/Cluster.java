package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.webgraph.*;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Cluster the given symmetric and loop-less graph
 */
public class Cluster implements Iterable<long[]> {
    private String basename;
    public ScatteredArcsASCIIGraph clusterGraph;
    public AtomicIntegerArray clusterLabels;
    public Int2IntOpenHashMap labelToNode;

    private class ClusterGraphIterator implements Iterator<long[]> {
        final static double DEFAULT_GAMMA = 1.;
        final static int SEED = 1984;

        private final NodeIterator nodes;
        private final AtomicIntegerArray labels;

        private final IntArrayFIFOQueue arcs = new IntArrayFIFOQueue();

        public ClusterGraphIterator() throws IOException {
            ImmutableGraph g = ImmutableGraph.load(basename);
            LayeredLabelPropagation llp = new LayeredLabelPropagation(g, SEED);

            this.labels = llp.computeLabels(DEFAULT_GAMMA, 1_000);
            this.nodes = g.nodeIterator();
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
    }

    @Override
    public Iterator<long[]> iterator() {
        try {
            return new ClusterGraphIterator();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void clusterize(String basename) throws IOException {
        this.basename = basename;
        ClusterGraphIterator cgi = (ClusterGraphIterator) this.iterator();

        this.clusterGraph = new ScatteredArcsASCIIGraph(cgi, false, false, 1_000_000, null, null);
        this.clusterLabels = cgi.labels;

        // When building the graph each node identifier is mapped to
        // a new node to improve efficiency, we need this association
        // saved to keep track of the right nodes.
        Int2IntOpenHashMap labelToNode = new Int2IntOpenHashMap();
        for (int i = 0; i < this.clusterGraph.ids.length; i++)
            labelToNode.put((int) this.clusterGraph.ids[i], i);
        this.labelToNode = labelToNode;

        System.out.println("Total clusters: " + labelToNode.size());
    }
}