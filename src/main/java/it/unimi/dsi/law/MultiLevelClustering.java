package it.unimi.dsi.law;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.*;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static it.unimi.dsi.law.Parameters.*;

/**
 * Repeat the clustering procedure LEVEL times and create a graph of the clusters.
 * Everytime operate on the previous cluster graph.
 */
public class MultiLevelClustering {
    public static final int LEVELS = 5;

    public static void main(String[] args) throws IOException {
        String previous = BASENAME_SYM;

        for (int i = 1; i <= LEVELS; i++) {
            System.out.println("Clustering level " + i);

            Cluster cluster = new Cluster();
            cluster.clusterize(previous);

            ImmutableGraph g = cluster.clusterGraph;
            AtomicIntegerArray labels = cluster.labels;

            File directory = new File(BASEDIR + "cluster-" + i);
            previous = directory + "/cluster-" + i;
            directory.mkdir();

            serialize(labels, directory + "/labels-" + i + ".atomicintegerarray");
            BVGraph.store(g, previous);
        }
    }

    private static void serialize(Object o, String filename) {
        try (FileOutputStream file = new FileOutputStream(filename);
             ObjectOutputStream out = new ObjectOutputStream(file)) {
                out.writeObject(o);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
