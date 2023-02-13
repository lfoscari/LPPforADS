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
import java.nio.file.Path;

import static it.unimi.dsi.law.Parameters.*;

/**
 * Compute offsets on the given graph, make the graph symmetric and remove loops.
 * All steps needed for clusterization.
 */
public class PrepareGraph {
    public static void main(String[] args) throws IOException {
        graph.toFile().mkdir();
        graphSymmetric.toFile().mkdir();

        computeOffsets(basename);
        ImmutableGraph graph = BVGraph.load(basename.toString());
        graph = Transform.symmetrize(graph, (ImmutableGraph) null);
        graph = Transform.filterArcs(graph, Transform.NO_LOOPS);
        BVGraph.store(graph, basenameSymmetric.toString());
        computeOffsets(basenameSymmetric);
    }

    static void computeOffsets(Path pathToGraph) {
        try {
            BVGraph.main(new String[]{"-o", "-O", "-L", pathToGraph.toString()});
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
