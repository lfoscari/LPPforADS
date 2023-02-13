package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ScatteredArcsASCIIGraph;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static it.unimi.dsi.law.Parameters.*;

/**
 * Repeat the clustering procedure LEVEL times and create a graph of the clusters.
 * Everytime operate on the previous cluster graph.
 */
public class MultiLevelClustering {
    public static final int LEVELS = 10;

    public static void main(String[] args) throws IOException {
        clusterDirectory.toFile().mkdir();
        multiLevelClustering(LEVELS, basenameSymmetric, clusterDirectory);
    }

    public static void multiLevelClustering(int levels, Path basename, Path clustersDirectory) throws IOException {
        Path previous = basename;

        for (int i = 1; i <= levels; i++) {
            System.out.println("Clustering level " + i);

            Cluster cluster = new Cluster();
            cluster.clusterize(previous);

            ScatteredArcsASCIIGraph graph = cluster.clusterGraph;
            AtomicIntegerArray labels = cluster.clusterLabels;
            Int2IntOpenHashMap nodeToNode = cluster.nodeToNode;
            Int2FloatOpenHashMap clusterSize = cluster.clusterSize;
            int graphRadius = cluster.graphRadius;

            Path directory = clustersDirectory.resolve("cluster-" + i);
            directory.toFile().mkdir();

            // Technically I don't need labels because if two nodes will be in the same cluster at the next level,
            // the map nodeToNode will return the same node, because it's a node -> label -> node map.
            BinIO.storeObject(labels, directory.resolve("cluster.labels").toFile());
            BinIO.storeObject(nodeToNode, directory.resolve("cluster.nodemap").toFile());
            BinIO.storeObject(clusterSize, directory.resolve("cluster.clustersize").toFile());
            BinIO.storeObject(graphRadius, directory.resolve("radius.int").toFile());
            BVGraph.store(graph, directory.resolve("cluster").toString());

            previous = directory.resolve("cluster");
        }
    }
}
