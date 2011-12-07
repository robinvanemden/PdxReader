/*
 * PdxReaderCmd.java - 1.0
 *
 * Copyright (c) 1998-1999 Robin van Emden - robin@pwy.nl
 *
 * nl.pwy.pdxreader.PdxReader parses Paradox DB files.
 * Should work with versions 3 through 7.
 *
 * The PdxReadCmd class enables you to test the class from the commandline:
 * java -jar PdxReader.jar <filename.db>
 * This should echo all fields to the terminal.
 *
 * Fileformat information: Randy Beck.
 *
 * nl.pwy.pdxreader.PdxReader is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 */

import nl.pwy.pdxreader.PdxReader;

/**
 * @author Robin van Emden
 */
public class PdxReaderCmd {

    private static PdxReader pdxReader;

    public static void main(String[] args) {

        if (args.length != 1 || args[0] == null || args[0].equals("")) {
            System.err.println('\n' + "usage:   java -jar PdxReader.jar <filename.db> ");
            System.exit(1);
        }

        String pdxFile;
        pdxFile = args[0];
        pdxReader = new PdxReader(pdxFile);
        writeTable();

        System.exit(1);
    }

    /**
     * Writes table to system
     */
    public static void writeTable() {
        for (int j = 1; j <= pdxReader.getNumRecords() + 1; j++) {
            for (int i = 1; i <= pdxReader.getNumFields(); i++) {
                System.out.println(pdxReader.getData()[i][j]);
            }
        }
    }

}