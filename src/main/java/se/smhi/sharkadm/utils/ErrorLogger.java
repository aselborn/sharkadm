package se.smhi.sharkadm.utils;

/*
 * SHARKadm - Administration of marine environmental monitoring data.
 * Contact: shark@smhi.se
 * Copyright (c) 2006-2017 SMHI, Swedish Meteorological and Hydrological Institute.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Use ErrorLogger.println instead of System.out.println
 * if you want the console logging also to be put in a text file.
 */
public class ErrorLogger {
    private static String logPath = "shark_adm_log.txt"; // Default path and file.
    private static PrintStream printStream = null;

    public static void setLogFilePath(String path) {
        logPath = path;
    }

    public static void println(String text) {
        if (printStream == null) {
            try {
                printStream = new PrintStream(new FileOutputStream(new File(logPath)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        // Add to log file.
        printStream.println(text);
    }

    public static void print(String text) {
        if (printStream == null) {
            try {
                printStream = new PrintStream(new FileOutputStream(new File(logPath)));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        // Add to log file.
        printStream.print(text);
    }

}

