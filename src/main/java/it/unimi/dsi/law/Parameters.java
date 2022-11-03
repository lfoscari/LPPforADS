package it.unimi.dsi.law;

import java.util.concurrent.TimeUnit;

public class Parameters {
    public static final String BASEDIR = "src/main/resources/";
    public static final String CLUSTERDIR = BASEDIR + "clusters/";
    public static final String BASENAME = BASEDIR + "eu-2005";
    public static final String BASENAME_SYM = BASEDIR + "eu-2005-sym";


    public static final long LOG_INTERVAL = 2;
    public static final TimeUnit LOG_UNIT = TimeUnit.SECONDS;
}
