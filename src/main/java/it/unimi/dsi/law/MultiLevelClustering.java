package it.unimi.dsi.law;

import com.google.common.primitives.Longs;
import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
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
    public static void main(String[] args) throws IOException {
        (new File(CLUSTERDIR)).mkdir();
        multiLevelCluster(10, BASENAME_SYM, CLUSTERDIR);
    }

    public static void multiLevelCluster(int levels, String basename, String clustersDirectory) throws IOException {
        String previous = basename;

        for (int i = 1; i <= levels; i++) {
            System.out.println("Clustering level " + i);

            Cluster cluster = new Cluster();
            cluster.clusterize(previous);

            ScatteredArcsASCIIGraph g = cluster.clusterGraph;
            AtomicIntegerArray labels = cluster.clusterLabels;
            Int2IntOpenHashMap nodeToNode = cluster.nodeToNode;
            Int2FloatOpenHashMap clusterSize = cluster.clusterSize;
            int graphRadius = cluster.graphRadius;

            File directory = new File(clustersDirectory + "cluster-" + i);
            String current = directory + "/cluster";
            directory.mkdir();

            // Technically I don't need labels because if two nodes will be in the same cluster at the next level,
            // the map nodeToNode will return the same node, because it's a node -> label -> node map.
            serialize(labels, directory + "/cluster.labels");
            serialize(nodeToNode, directory + "/cluster.nodemap");
            serialize(clusterSize, directory + "/cluster.clustersize");
            serialize(graphRadius, directory + "/radius.int");
            BVGraph.store(g, directory + "/cluster");

            previous = current;
        }
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
