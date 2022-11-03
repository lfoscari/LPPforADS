package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.algo.ParallelBreadthFirstVisit;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static it.unimi.dsi.law.MultiLevelClustering.deserialize;
import static it.unimi.dsi.law.Parameters.*;

public class DistanceSketch {
    final static XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom();
    final static int nodes = 862664;

    public static void main(String[] args) throws IOException {
        ImmutableGraph original = BVGraph.load(BASENAME_SYM);
        float meanError = 0;

        for (int i = 0; i < 100; i++) {
            int from = random.nextInt(nodes);
            int to = from + random.nextInt(1000);

            int distance = bfsDistance(original, from, to);
            if (distance == -1)
                return;

            float approxDistance = sketchDistance(from, to, 10);
            meanError = (meanError * i + (Math.abs(distance - approxDistance))) / (i + 1);

            System.out.println("bfs " + distance + "\t" + "cluster " + approxDistance);
        }

        System.out.println("Mean error:" + meanError);
    }

    public static float sketchDistance(int from, int to, int levels) throws IOException {
        if (from == to)
            return 0f;

        for (int level = 1; level < levels; level++) {
            String directory = CLUSTERDIR + "cluster-" + level + "/";
            AtomicIntegerArray labels = (AtomicIntegerArray) deserialize(directory + "cluster.labels");

            int label_from = labels.get(from);
            int label_to = labels.get(to);

            if (label_from == label_to)
                return sketchDistanceAtLevel(level - 1, from, to);

            Int2IntOpenHashMap nodeToNode = (Int2IntOpenHashMap) deserialize(directory + "cluster.nodemap");

            from = nodeToNode.get(label_from);
            to = nodeToNode.get(label_to);
        }

        return sketchDistanceAtLevel(levels, from, to);
    }

    private static float sketchDistanceAtLevel(int level, int from, int to) throws IOException {
        ImmutableGraph graph = loadLevel(level);

        int distance = bfsDistance(graph, from , to);
        float clusterRadius = clusterRadiusAtLevel(level - 1, from);

        return distance + (clusterRadius * 2);
    }

    private static float clusterRadiusAtLevel(int level, int from) {
        if (level == 0)
            return 0f;

        Int2FloatOpenHashMap clusterSizes = (Int2FloatOpenHashMap) deserialize(CLUSTERDIR + "cluster-" + level + "/cluster.clustersize");
        int graphRadius = (int) deserialize(CLUSTERDIR + "cluster-" + level + "/radius.int");

        return clusterSizes.get(from) * graphRadius;
    }

    private static ImmutableGraph loadLevel(int level) throws IOException {
        if (level == 0) {
            return BVGraph.load(BASENAME_SYM);
        }

        return BVGraph.load(CLUSTERDIR + "cluster-" + level + "/cluster");
    }

    public static int bfsDistance(ImmutableGraph g, int from, int to) {
        ParallelBreadthFirstVisit bfs =
                new ParallelBreadthFirstVisit(g, 0, false, null);
        bfs.visit(from);

        int distance = -1;

        for (int i = 0; i < bfs.cutPoints.size() - 1; i++) {
            int min = bfs.cutPoints.getInt(i);
            int max = bfs.cutPoints.getInt(i + 1);

            if (bfs.queue.subList(min, max).contains(to)) {
                distance = i;
            }
        }

        return distance;
    }
}
