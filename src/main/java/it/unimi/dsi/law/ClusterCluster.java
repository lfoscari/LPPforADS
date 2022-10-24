package it.unimi.dsi.law;

import it.unimi.dsi.law.graph.LayeredLabelPropagation;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.ImmutableGraph;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class ClusterCluster {
    public static void main(String[] args) throws IOException {
        String basedir = "src/main/resources/";
        ProgressLogger progress = new ProgressLogger();

        ImmutableGraph g = ImmutableGraph.load(basedir + "eu-2005-sym", progress);

        LayeredLabelPropagation llp = new LayeredLabelPropagation(g, 1984);
        final AtomicIntegerArray labels = llp.computeLabels(0.00000001);

        HashMap<Integer, Integer> cluster = new HashMap<>();
        for (int i = 0; i < labels.length(); i++) {
            int c = labels.get(i);
            cluster.put(c, cluster.getOrDefault(c, 0) + 1);
        }
        cluster.forEach((k, v) -> System.out.println(k + " => " + v));
        System.out.println("Total: " + cluster.keySet().size());
    }
}