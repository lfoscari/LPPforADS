package it.unimi.dsi.law;

import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ScatteredArcsASCIIGraph;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static it.unimi.dsi.law.Parameters.*;

/**
 * Repeat the clustering procedure LEVEL times and create a graph of the clusters.
 * Everytime operate on the previous cluster graph.
 */
public class MultiLevelClustering {
    public static final int LEVELS = 50;

    public static void main(String[] args) throws IOException {
        String previous = BASENAME_SYM;

        for (int i = 1; i <= LEVELS; i++) {
            System.out.println("Clustering level " + i);

            Cluster cluster = new Cluster();
            cluster.clusterize(previous);

            ScatteredArcsASCIIGraph g = cluster.clusterGraph;
            AtomicIntegerArray labels = cluster.clusterLabels;
            Int2IntOpenHashMap nodeToNode = cluster.nodeToNode;

            File directory = new File(BASEDIR + "cluster-" + i);
            previous = directory + "/cluster-" + i;
            directory.mkdir();

            // Technically I don't need labels because if two nodes will be in the same cluster at the next level,
            // the map nodeToNode will return the same node, because it's a node -> label -> node map.
            serialize(labels, directory + "/labels-" + i + ".integerarray");
            serialize(nodeToNode, directory + "/node2node-" + i + ".openhashmap");
            BVGraph.store(g, previous);
        }
    }

    public static Int2IntOpenHashMap computeNodeClusterMap(Int2IntOpenHashMap labelToNode, AtomicIntegerArray labels) {
        // Create a minimal perfect hashing function (find a better alternative than a hashmap)
        Int2IntOpenHashMap map = new Int2IntOpenHashMap(labels.length(), 0.9999999f);
        for (int i = 0; i < labels.length(); i++)
            map.put(i, labelToNode.get(labels.get(i)));
        return map;
    }

    public static void serialize(Object o, String filename) {
        try (FileOutputStream file = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(file)) {
            out.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Object deserialize(String filename) {
        try (FileInputStream file = new FileInputStream(filename);
             ObjectInputStream out = new ObjectInputStream(file)) {
            return out.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
