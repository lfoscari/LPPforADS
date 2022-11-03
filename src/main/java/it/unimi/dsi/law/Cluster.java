package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntArrayFIFOQueue;
import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.webgraph.*;
import it.unimi.dsi.webgraph.algo.SumSweepUndirectedDiameterRadius;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicIntegerArray;

/**
 * Cluster the given symmetric and loop-less graph
 */
public class Cluster {
    public ScatteredArcsASCIIGraph clusterGraph;
    public AtomicIntegerArray clusterLabels;
    public Int2IntOpenHashMap nodeToNode;
    public Int2FloatOpenHashMap clusterSize;
    public int graphRadius;

    public void clusterize(String basename) throws IOException {
        ImmutableGraph graph = BVGraph.load(basename);
        ClusterGraphIterator cgi = new ClusterGraphIterator(graph);

        this.clusterGraph = new ScatteredArcsASCIIGraph(cgi.iterator(), false, false, 1_000_000, null, null);
        this.clusterLabels = cgi.labels;

        // When building the graph each node identifier (a label at the previous level)
        // is mapped to a new node to improve efficiency, we need this association
        // saved to keep track of the right nodes.

        Int2IntOpenHashMap labelToNode = new Int2IntOpenHashMap(this.clusterGraph.ids.length, 0.9999999f);
        for (int i = 0; i < this.clusterGraph.ids.length; i++) {
            labelToNode.put((int) this.clusterGraph.ids[i], i);
        }

        this.nodeToNode = new Int2IntOpenHashMap(cgi.labels.length(), 0.9999999f);
        this.clusterSize = new Int2FloatOpenHashMap(cgi.labels.length(), 0.9999999f);
        this.clusterSize.defaultReturnValue(0f);

        for (int i = 0; i < cgi.labels.length(); i++) {
            int label = cgi.labels.get(i);
            this.nodeToNode.put(i, labelToNode.get(label));
            this.clusterSize.addTo(label, 1f);
        }

        this.clusterSize.trim();

        // Normalize cluster sizes
        for (int i = 0; i < cgi.labels.length(); i++) {
            int label = cgi.labels.get(i);
            this.clusterSize.compute(label, (k, v) -> v / cgi.labels.length());
        }

        // To estimate the radius of each cluster we can assume that they are of the same
        // size and compute their radius by dividing the radius of the original graph
        // by the number of clusters.
        // In graphs the diameters is two times the radius?
        // https://webgraph.di.unimi.it/docs/it/unimi/dsi/webgraph/algo/SumSweepUndirectedDiameterRadius.html
        // Will it work? Who knows...

        SumSweepUndirectedDiameterRadius radius = new SumSweepUndirectedDiameterRadius(this.clusterGraph, SumSweepUndirectedDiameterRadius.OutputLevel.RADIUS, null);
        radius.compute();
        graphRadius = radius.getRadius();

        System.out.println("Graph radius: " + radius.getRadius());
        System.out.println("Total clusters: " + labelToNode.size());
    }
}