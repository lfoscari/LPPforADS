package it.unimi.dsi.law;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.File;
import java.io.IOException;

import static it.unimi.dsi.law.Parameters.*;

public class MultiLevelClustering {
    static final int LEVELS = 5;

    public static void main(String[] args) throws IOException {
        String previous = BASEDIR + BASENAME;

        for (int i = 1; i <= LEVELS; i++) {
            System.out.println("Clustering level " + i);
            ImmutableGraph g = Cluster.clusterize(previous);

            File directory = new File(BASEDIR + "cluster-" + i);
            previous = directory + "/" + BASENAME + "-cluster-" + i;
            directory.mkdir();

            BVGraph.store(g, previous);
        }
    }
}
