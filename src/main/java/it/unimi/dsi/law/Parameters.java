package it.unimi.dsi.law;

import java.nio.file.Path;

public class Parameters {
    public static final Path resources = Path.of("src/main/resources/");

    public static final Path graph = resources.resolve("graph");
    public static final Path graphSymmetric = resources.resolve("graph-symmetric");
    public static final Path clusterDirectory = resources.resolve("clusters");

    public static final Path basename = graph.resolve("enwiki-2022");
    public static final Path basenameSymmetric = graphSymmetric.resolve("enwiki-2022");
}
