package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
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
    public static void main(String[] args) throws IOException {
        clusterDirectory.toFile().mkdir();
        multiLevelCluster(10, basenameSymmetric, clusterDirectory);
    }

    public static void multiLevelCluster(int levels, Path basename, Path clustersDirectory) throws IOException {
        Path previous = basename;

        for (int i = 1; i <= levels; i++) {
            System.out.println("Clustering level " + i);

            Cluster cluster = new Cluster();
            cluster.clusterize(previous);

            ScatteredArcsASCIIGraph g = cluster.clusterGraph;
            AtomicIntegerArray labels = cluster.clusterLabels;
            Int2IntOpenHashMap nodeToNode = cluster.nodeToNode;
            Int2FloatOpenHashMap clusterSize = cluster.clusterSize;
            int graphRadius = cluster.graphRadius;

            Path directory = Path.of(clustersDirectory + "cluster-" + i);
            Path current = directory.resolve("cluster");
            directory.toFile().mkdir();

            // Technically I don't need labels because if two nodes will be in the same cluster at the next level,
            // the map nodeToNode will return the same node, because it's a node -> label -> node map.
            serialize(labels, directory.resolve("cluster.labels"));
            serialize(nodeToNode, directory.resolve("cluster.nodemap"));
            serialize(clusterSize, directory.resolve("cluster.clustersize"));
            serialize(graphRadius, directory.resolve("radius.int"));
            BVGraph.store(g, directory.resolve("cluster").toString());

            previous = current;
        }
    }

    public static void serialize(Object o, Path filename) {
        try (FileOutputStream file = new FileOutputStream(filename.toFile());
             ObjectOutputStream out = new ObjectOutputStream(file)) {
            out.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object deserialize(Path filename) {
        try (FileInputStream file = new FileInputStream(filename.toFile());
             ObjectInputStream out = new ObjectInputStream(file)) {
            return out.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
