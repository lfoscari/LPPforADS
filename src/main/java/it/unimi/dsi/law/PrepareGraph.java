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
 * All steps needed for clusterization. (you may need to adjust the BATCH_SIZE parameter)
 */
public class PrepareGraph {
    public static final int BATCH_SIZE = 1_000_000;

    public static void main(String[] args) throws JSAPException, IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        ProgressLogger progress = new ProgressLogger(LoggerFactory.getLogger("preparation"));

        graph.toFile().mkdir();
        graphSymmetric.toFile().mkdir();

        BVGraph.main(new String[] {"-o", "-O", "-L", basename.toString()});

        ImmutableGraph g = BVGraph.load(basename.toString(), progress);
        g = Transform.symmetrizeOffline(g, BATCH_SIZE, resources.toFile(), progress);
        g = Transform.filterArcs(g, Transform.NO_LOOPS, progress);
        BVGraph.store(g, basenameSymmetric.toString(), progress);
    }
}
