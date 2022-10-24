package it.unimi.dsi.law;

import com.martiansoftware.jsap.JSAPException;
import it.unimi.dsi.webgraph.BVGraph;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Offsets {
    public static void main(String[] args) throws JSAPException, IOException, ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException, InstantiationException {
        String basedir = "src/main/resources/";

        BVGraph.main(("-o -O -L " + basedir + "eu-2005").split(" "));
    }
}
