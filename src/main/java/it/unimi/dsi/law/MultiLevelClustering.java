package it.unimi.dsi.law;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.File;
import java.io.IOException;

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

            Cluster cluster = new Cluster(previous);
            ImmutableGraph g = cluster.clusterize();

            File directory = new File(BASEDIR + "cluster-" + i);
            previous = directory + "/cluster-" + i;
            directory.mkdir();

            BVGraph.store(g, previous);
        }
    }
}
