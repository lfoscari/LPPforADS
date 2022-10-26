package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static it.unimi.dsi.law.Parameters.*;

/**
 * Clusterize the given symmetric and loop-less graph
 */
public class Cluster implements Iterable<long[]> {
    private final String basename;

    public Cluster(String basename) {
        this.basename = basename;
    }

    private class ClusterGraphIterator implements Iterator<long[]> {
        final static double DEFAULT_GAMMA = 0.0078125; // Picked randomly from DEFAULT_GAMMAS
        final static int SEED = 1984;

        private final NodeIterator nodes;
        private final AtomicIntegerArray labels;

        private final IntArrayFIFOQueue arcs = new IntArrayFIFOQueue();

        public ClusterGraphIterator() throws IOException {
            ImmutableGraph g = ImmutableGraph.load(basename);
            LayeredLabelPropagation llp = new LayeredLabelPropagation(g, SEED);

            this.labels = llp.computeLabels(DEFAULT_GAMMA);
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

    public ImmutableGraph clusterize() throws IOException {
        return new ScatteredArcsASCIIGraph(iterator(), false, false, 1_000_000, null, null);
    }
}