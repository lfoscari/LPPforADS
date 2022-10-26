package it.unimi.dsi.law;

import com.martiansoftware.jsap.JSAPException;
import it.unimi.dsi.logging.ProgressLogger;
import it.unimi.dsi.webgraph.BVGraph;
import it.unimi.dsi.webgraph.ImmutableGraph;
import it.unimi.dsi.webgraph.Transform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import static it.unimi.dsi.law.Parameters.*;

/**
 * Compute offsets on the given graph, make the graph symmetric and remove loops.
 * All steps needed for clusterization.
 * (you may need to adjust the BATCH_SIZE parameter)
 */
public class PrepareGraph {
    public static final int BATCH_SIZE = 1_000_000;

    public static void main(String[] args) throws JSAPException, IOException, ClassNotFoundException,
            InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        Logger l = LoggerFactory.getLogger("preparation");
        ProgressLogger pl = new ProgressLogger(l, LOG_INTERVAL, LOG_UNIT);

        BVGraph.main(new String[] {"-o", "-O", "-L", BASENAME});

        ImmutableGraph g = BVGraph.load(BASENAME, pl);
        g = Transform.symmetrizeOffline(g, BATCH_SIZE, null, pl);
        g = Transform.filterArcs(g, Transform.NO_LOOPS, pl);
        BVGraph.store(g, BASENAME_SYM, pl);
    }
}
