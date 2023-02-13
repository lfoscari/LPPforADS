package it.unimi.dsi.law;

import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.algo.HyperBall;

import java.io.IOException;

import static it.unimi.dsi.law.Parameters.basenameSymmetric;

public class HyperBallBaseline {
    public static final int SEED = 1984;

    public static void main(String[] args) throws IOException {
        ImmutableGraph graph = BVGraph.load(basenameSymmetric.toString());
        try (HyperBall hb = new HyperBall(graph, 10)) {
            hb.init(SEED);
            hb.iterate();
        }
    }
}
