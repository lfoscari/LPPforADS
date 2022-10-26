package it.unimi.dsi.law;

import it.unimi.dsi.fastutil.ints.Int2IntOpenHashMap;
import it.unimi.dsi.util.XoRoShiRo128PlusPlusRandom;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.algo.ParallelBreadthFirstVisit;

import java.io.IOException;

import static it.unimi.dsi.law.MultiLevelClustering.LEVELS;
import static it.unimi.dsi.law.MultiLevelClustering.deserialize;
import static it.unimi.dsi.law.Parameters.*;

public class DistanceSketch {
    final static XoRoShiRo128PlusPlusRandom random = new XoRoShiRo128PlusPlusRandom();
    final static int nodes = 862664;

    public static void main(String[] args) throws IOException {
        ImmutableGraph original = BVGraph.load(BASENAME_SYM);

        for (int i = 0; i < 100; i++) {
            int from = random.nextInt(nodes);
            int to = random.nextInt(nodes);

            int distance = bfsDistance(original, from, to);
            if (distance == -1) return;

            System.out.println(
                    "bfs " + distance + "\t" +
                    "cluster " + sketchDistance(from, to, 1, original)
            );
        }
    }

    public static int sketchDistance(int from, int to, int level, ImmutableGraph previous) throws IOException {
        String directory = BASEDIR + "cluster-" + level + "/";
        Int2IntOpenHashMap labels = (Int2IntOpenHashMap) deserialize(directory + "labels-" + level + ".openhashmap");
        ImmutableGraph current = BVGraph.load(directory + "cluster-" + level);

        int label_from = labels.get(from);
        int label_to = labels.get(to);

        if (label_from == label_to) {
            System.out.print("[" + level + "]\t");
            return bfsDistance(previous, from, to);
        }

        if (level + 1 > LEVELS) {
            System.out.print("[" + level + "]\t");
            return bfsDistance(current, label_from, label_to);
        }

        return sketchDistance(label_from, label_to, level + 1, current);
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