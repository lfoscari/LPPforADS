package it.unimi.dsi.law;

import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.algo.HyperBall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static it.unimi.dsi.law.Parameters.BASENAME_SYM;

public class HyperBallBaseline {
    public static final int SEED = 1984;

    public static void main(String[] args) throws IOException {
        ImmutableGraph graph = BVGraph.load(BASENAME_SYM);
        try (HyperBall hb = new HyperBall(graph, 10)) {
            hb.init(SEED);
            hb.iterate();
        }
    }
}
