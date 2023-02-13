package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.Int2FloatOpenHashMap;
import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.fastutil.io.BinIO;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.algo.ParallelBreadthFirstVisit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.util.concurrent.atomic.AtomicIntegerArray;

import static it.unimi.dsi.law.MultiLevelClustering.LEVELS;
import static it.unimi.dsi.law.Parameters.*;

public class DistanceSketch {
    final static XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom();

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        ImmutableGraph original = BVGraph.load(basenameSymmetric.toString());
        float meanError = 0;

        for (int i = 0; i < 100; i++) {
            int from = random.nextInt(original.numNodes());
            int to = from + random.nextInt(1000);

            int distance = bfsDistance(original, from, to);
            if (distance == -1) {
                return;
            }

            float approxDistance = sketchDistance(from, to, LEVELS);
            meanError = (meanError * i + (Math.abs(distance - approxDistance))) / (i + 1);

            System.out.println("bfs " + distance + "\t" + "cluster " + approxDistance);
        }

        System.out.println("Mean error:" + meanError);
    }

    public static float sketchDistance(int from, int to, int levels) throws IOException, ClassNotFoundException {
        if (from == to) {
            return 0f;
        }

        for (int level = 1; level < levels; level++) {
            Path directory = clusterDirectory.resolve("cluster-" + level);
            AtomicIntegerArray labels = (AtomicIntegerArray) BinIO.loadObject(directory.resolve("cluster.labels").toFile());

            int label_from = labels.get(from);
            int label_to = labels.get(to);

            if (label_from == label_to) {
                return sketchDistanceAtLevel(level - 1, from, to);
            }

            Int2IntOpenHashMap nodeToNode = (Int2IntOpenHashMap) BinIO.loadObject(directory.resolve("cluster.nodemap").toFile());

            from = nodeToNode.get(label_from);
            to = nodeToNode.get(label_to);
        }

        return sketchDistanceAtLevel(levels, from, to);
    }

    private static float sketchDistanceAtLevel(int level, int from, int to) throws IOException, ClassNotFoundException {
        ImmutableGraph graph = loadLevel(level);

        int distance = bfsDistance(graph, from , to);
        float clusterRadius = clusterRadiusAtLevel(level - 1, from);

        return distance + (clusterRadius * 2);
    }

    private static float clusterRadiusAtLevel(int level, int from) throws IOException, ClassNotFoundException {
        if (level == 0) {
            return 0f;
        }

        Int2FloatOpenHashMap clusterSizes = (Int2FloatOpenHashMap) BinIO.loadObject(clusterDirectory.resolve("cluster-" + level).resolve("cluster.clustersize").toFile());
        int graphRadius = (int) BinIO.loadObject(clusterDirectory.resolve("cluster-" + level).resolve("radius.int").toFile());

        return clusterSizes.get(from) * graphRadius;
    }

    private static ImmutableGraph loadLevel(int level) throws IOException {
        if (level == 0) {
            return BVGraph.load(basenameSymmetric.toString());
        }

        return BVGraph.load(clusterDirectory.resolve("cluster-" + level).resolve("cluster").toString());
    }

    public static int bfsDistance(ImmutableGraph g, int from, int to) {
        ParallelBreadthFirstVisit bfs = new ParallelBreadthFirstVisit(g, 0, false, null);
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
