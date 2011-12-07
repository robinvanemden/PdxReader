/*
 * PdxReaderCmd.java - 1.0
 *
 * Copyright (c) 1998-1999 Robin van Emden - robin@pwy.nl
 *
 * nl.pwy.pdxreader.PdxReader parses Paradox DB files.
 * Should work with versions 3 through 7.
 *
 * Fileformat information: Randy Beck.
 *
 * nl.pwy.pdxreader.PdxReader is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 */

package nl.pwy.pdxreader;

import java.io.*;

/**
 * @author Robin van Emden
 */
public class PdxReader {
    private static final byte pxfAlpha = 1;
    private static final byte pxfDate = 2;
    private static final byte pxfShort = 3;
    private static final byte pxfLong = 4;
    private static final byte pxfCurrency = 5;
    private static final byte pxfNumber = 6;
    private static final byte pxfLogical = 9;
    private static final byte pxfMemoBLOb = 12;
    private static final byte pxfBLOb = 13;
    private static final byte pxfFmtMemoBLOb = 14;
    private static final byte pxfOLE = 15;
    private static final byte pxfGraphic = 16;
    private static final byte pxfTime = 20;
    private static final byte pxfTimestamp = 21;
    private static final byte pxfAutoInc = 22;
    private static final byte pxfBCD = 23;
    private static final byte pxfBytes = 24;

    private int recordSize;
    private int headerSize;
    private int maxTableSize;
    private int numRecords;
    private int fileBlocks;
    private int numFields;
    private byte[] data_in;
    private int[] fType;
    private int[] fSize;
    private int fileData;
    private String[][] tableStringArray;
    private int counterRecords = 1;
    private final BigDate bDate = new BigDate();


    /**
     * Constructor, needs Paradox DB filename
     *
     * @param filename
     */
    public PdxReader(String filename) {
        fileToArray(filename);
        for (int dBlock = 0; dBlock < fileBlocks; dBlock++) {
            dataBlockReader(dBlock);
        }
    }


    /**
     * Converts PDX file to array
     *
     * @param filename
     */

    private void fileToArray(String filename) {
        File file;
        FileInputStream file_in = null;
        try {
            file = new File(System.getProperty("user.dir") + File.separator + filename);
            if (!file.exists()) {
                file = new File(filename);
            }
            file_in = new FileInputStream(file);
            int size = (int) file.length();
            data_in = new byte[size];
            file_in.read(data_in);
        } catch (IOException e) {
            System.out.println("IOException :" + e);
        } finally {
            if (file_in != null) try {
                file_in.close();
                headerReader();
            } catch (IOException e) {
                System.out.println("IOException :" + e);
            }
        }
    }

    /**
     * Reads PDX header
     */
    private void headerReader() {
        LEDataInputStream array_in = new LEDataInputStream(new ByteArrayInputStream(data_in));

        try {
            recordSize = array_in.readUnsignedShort();
            headerSize = array_in.readUnsignedShort();
            int fileType = array_in.readUnsignedByte();
            maxTableSize = array_in.readUnsignedByte();
            numRecords = array_in.readInt();
            int nextBlock = array_in.readUnsignedShort();
            fileBlocks = array_in.readUnsignedShort();
            int firstBlock = array_in.readUnsignedShort();
            int lastBlock = array_in.readUnsignedShort();
            int unknown12x13 = array_in.readUnsignedShort();
            int modifiedFlags1 = array_in.readUnsignedByte();
            int indexFieldNumber = array_in.readUnsignedByte();
            int primaryIndexWorkspace = array_in.skipBytes(4);
            int unknownPtr1A = array_in.skipBytes(4);
            int unknown1Ex20 = array_in.skipBytes(3);
            numFields = array_in.readShort();
            int primaryKeyFields = array_in.readShort();
            int encryption1 = array_in.readInt();
            int sortOrder = array_in.readUnsignedByte();
            int modifiedFlags2 = array_in.readUnsignedByte();
            int unknown2Bx2C = array_in.skipBytes(2);
            int changeCount1 = array_in.readUnsignedByte();
            int changeCount2 = array_in.readUnsignedByte();
            int unknown2F = array_in.readUnsignedByte();
            int tableNamePtrPtr = array_in.skipBytes(4);
            int fldInfoPtr = array_in.skipBytes(4);
            int writeProtected = array_in.readUnsignedByte();
            int fileVersionID = array_in.readUnsignedByte();
            int maxBlocks = array_in.readUnsignedShort();
            int unknown3C = array_in.readUnsignedByte();
            int auxPasswords = array_in.readUnsignedByte();
            int unknown3Ex3F = array_in.skipBytes(2);
            int cryptInfoStartPtr = array_in.skipBytes(4);
            int cryptInfoEndPtr = array_in.skipBytes(4);
            int unknown48 = array_in.readUnsignedByte();
            int autoInc = array_in.readInt();
            int unknown4Dx4E = array_in.skipBytes(2);
            int indexUpdateRequired = array_in.readUnsignedByte();
            int unknown50x54 = array_in.skipBytes(5);
            int refIntegrity = array_in.readUnsignedByte();
            int unknown56x57 = array_in.skipBytes(2);
            byte[] fieldInfo35 = new byte[512];
            byte[] fieldInfo = new byte[512];
            tableStringArray = new String[numFields + 1][numRecords + 2];
            switch (fileType) {
                case 1:
                case 4:
                case 6:
                case 7:
                case 8: {
                    fSize = new int[numFields + 1];
                    fType = new int[numFields + 1];
                    for (int i = 1; i <= numFields; i++) {
                        fType[i] = array_in.readUnsignedByte();
                        fSize[i] = array_in.readUnsignedByte();
                    }
                    array_in.skipBytes(4);
                    array_in.skipBytes(numFields * 4);
                    break;
                }
                default: {
                    if (fileVersionID <= 4) {
                        fSize = new int[numFields + 1];
                        fType = new int[numFields + 1];
                        for (int i = 1; i <= numFields; i++) {
                            fType[i] = array_in.readUnsignedByte();
                            fSize[i] = array_in.readUnsignedByte();
                        }
                        array_in.skipBytes(83 + (numFields * 4));
                        StringBuffer sb;
                        for (int i = 1; i <= numFields; i++) {
                            int bt = 1;
                            sb = new StringBuffer();
                            while (bt != 0) {
                                bt = array_in.readUnsignedByte();
                                sb.append((char) bt);
                            }
                            tableStringArray[i][1] = sb.toString();
                        }
                    } else {
                        int fileVerID2 = array_in.readShort();
                        int fileVerID3 = array_in.readShort();
                        int encryption2 = array_in.readInt();
                        int fileUpdateTime = array_in.readInt();
                        int hiFieldID = array_in.readShort();
                        int hiFieldIDinfo = array_in.readShort();
                        int sometimesNumFields = array_in.readShort();
                        int dosCodePage = array_in.readShort();
                        int unknown6Cx6F = array_in.skipBytes(4);
                        int changeCount4 = array_in.readShort();
                        int unknown72x77 = array_in.skipBytes(6);
                        fSize = new int[numFields + 1];
                        fType = new int[numFields + 1];
                        for (int i = 1; i <= numFields; i++) {
                            fType[i] = array_in.readUnsignedByte();
                            fSize[i] = array_in.readUnsignedByte();
                        }
                        array_in.skipBytes(83 + (numFields * 4));
                        StringBuffer sb;
                        for (int i = 1; i <= numFields; i++) {
                            int bt;
                            sb = new StringBuffer();
                            while ((bt = array_in.readUnsignedByte()) != 0) {
                                sb.append((char) bt);
                            }
                            tableStringArray[i][1] = sb.toString();
                        }
                    }
                    break;
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (array_in != null) try {
                array_in.close();
            } catch (IOException ignored) {
            }
        }
    }


    /**
     * Reads PDX datablocks
     *
     * @param blockNo
     */
    private void dataBlockReader(int blockNo) {
        boolean IsEmpty;
        DataInputStream array_in = new DataInputStream(new ByteArrayInputStream(data_in));
        try {
            array_in.skipBytes(headerSize + (blockNo * (maxTableSize * 1024)));
            int nextBlock2 = array_in.readUnsignedShort();
            int blockNumber = array_in.readUnsignedShort();
            byte[] aByte = new byte[2];
            array_in.read(aByte);
            DataInputStream sTemp;
            LEDataInputStream leTemp = new LEDataInputStream(new ByteArrayInputStream(aByte));
            short addTemp = leTemp.readShort();
            int addDataSize = addTemp;
            int numRecsInBlock = ((addDataSize / recordSize) + 1);
            leTemp.close();
            for (int i = 1; i <= numRecsInBlock; i++) {
                counterRecords++;
                for (int j = 1; j <= numFields; j++) {
                    int counterFields = j;
                    switch (fType[j]) {
                        case pxfAlpha: {
                            StringBuffer tableStringBuffer = new StringBuffer();
                            int[] intBuffer = new int[fSize[j] + 1];
                            for (int k = 1; k <= fSize[j]; k++) {
                                intBuffer[k] = array_in.readUnsignedByte();
                                if (intBuffer[k] != 0) tableStringBuffer.append((char) intBuffer[k]);
                            }
                            tableStringArray[counterFields][counterRecords] = tableStringBuffer.toString();
                            break;
                        }
                        case pxfDate: {
                            byte[] bTemp = new byte[4];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 4; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            int tDateField = sTemp.readInt() - 719163;
                            bDate.set(tDateField);
                            tableStringArray[counterFields][counterRecords] = (bDate.getDD() + "-" + bDate.getMM() + "-" + bDate.getYYYY());
                            sTemp.close();
                            break;
                        }
                        case pxfShort: {
                            byte[] bTemp = new byte[2];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 2; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            short dTemp = sTemp.readShort();
                            tableStringArray[counterFields][counterRecords] = ("" + (dTemp));
                            sTemp.close();
                            break;
                        }

                        case pxfLong: {
                            byte[] bTemp = new byte[4];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 4; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            int dTemp = sTemp.readInt();
                            tableStringArray[counterFields][counterRecords] = ("" + (dTemp));
                            sTemp.close();
                            break;
                        }
                        case pxfCurrency: {
                            byte[] bTemp = new byte[8];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 8; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            double dTemp = sTemp.readDouble();
                            tableStringArray[counterFields][counterRecords] = ("$" + (dTemp));
                            sTemp.close();
                            break;
                        }
                        case pxfNumber: {
                            byte[] bTemp = new byte[8];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 8; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            double dTemp = sTemp.readDouble();
                            tableStringArray[counterFields][counterRecords] = ("" + (dTemp));
                            sTemp.close();
                            break;
                        }
                        case pxfLogical: {
                            int temp = array_in.readUnsignedByte();
                            if (temp == 0) tableStringArray[counterFields][counterRecords] = ("");
                            else {
                                temp ^= 0x80;
                                tableStringArray[counterFields][counterRecords] = ("" + (temp));
                            }
                            break;
                        }
                        case pxfMemoBLOb: {
                            int temp = array_in.skipBytes(fSize[j]);
                            temp ^= 0x80;
                            tableStringArray[counterFields][counterRecords] = ("<pxfMemoBLOb>");
                            break;
                        }
                        case pxfBLOb: {
                            int temp = array_in.skipBytes(fSize[j]);
                            temp ^= 0x80;
                            tableStringArray[counterFields][counterRecords] = ("<pxfBLOb>");
                            break;
                        }
                        case pxfFmtMemoBLOb: {
                            int temp = array_in.skipBytes(fSize[j]);
                            temp ^= 0x80;
                            tableStringArray[counterFields][counterRecords] = ("<pxfFmtMemoBLOb>");
                            break;
                        }
                        case pxfOLE: {
                            int temp = array_in.skipBytes(fSize[j]);
                            temp ^= 0x80;
                            tableStringArray[counterFields][counterRecords] = ("<pxfOLE>");
                            break;
                        }
                        case pxfGraphic: {
                            int temp = array_in.skipBytes(fSize[j]);
                            temp ^= 0x80;
                            tableStringArray[counterFields][counterRecords] = ("<pxfGraphic>");
                            break;
                        }
                        case pxfTime: {
                            byte[] bTemp = new byte[4];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 4; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            int dTemp = (sTemp.readInt() / 1000);
                            int uuTemp = (dTemp / 3600);
                            int mmTemp = ((dTemp - (uuTemp * 3600)) / 60);
                            int ssTemp = (dTemp - (uuTemp * 3600) - (mmTemp * 60));
                            tableStringArray[counterFields][counterRecords] = ("" + (uuTemp) + ":" + mmTemp + ":" + ssTemp);
                            sTemp.close();
                            break;
                        }
                        case pxfTimestamp: {
                            byte[] bTemp = new byte[8];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 8; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            long dTemp = sTemp.readLong();
                            tableStringArray[counterFields][counterRecords] = ("" + dTemp);
                            sTemp.close();
                            break;

                        }
                        case pxfAutoInc: {
                            byte[] bTemp = new byte[4];
                            array_in.read(bTemp);
                            IsEmpty = true;
                            for (int m = 0; m < 4; m++) {
                                if (bTemp[m] != 0) {
                                    IsEmpty = false;
                                    break;
                                }
                            }
                            if (IsEmpty) {
                                tableStringArray[counterFields][counterRecords] = ("");
                                break;
                            }
                            bTemp[0] ^= 0x80;
                            sTemp = new DataInputStream(new ByteArrayInputStream(bTemp));
                            int dTemp = sTemp.readInt();
                            tableStringArray[counterFields][counterRecords] = ("" + (dTemp));
                            sTemp.close();
                            break;
                        }
                        case pxfBCD: {
                            int temp = array_in.skipBytes(17);
                            temp ^= 0x80;
                            tableStringArray[counterFields][counterRecords] = ("<pxfBCD>");
                            break;
                        }
                        case pxfBytes: {
                            int temp = array_in.skipBytes(fSize[j]);
                            temp ^= 0x80;
                            tableStringArray[counterFields][counterRecords] = ("<pxfBytes>");
                            break;
                        }
                    }
                }
            }
        } catch (IOException ignored) {
        } finally {
            if (array_in != null) try {
                array_in.close();
            } catch (IOException ignored) {
            }
        }

    }

    public int getNumFields() {
        return numFields;
    }

    public int getNumRecords() {
        return numRecords;
    }

    public String[][] getData() {
        return tableStringArray;
    }

    public int[] getFieldSize() {
        int[] maxFSize = new int[numFields + 1];
        for (int i = 1; i <= numFields; i++) {
            if (fType[i] == 1) maxFSize[i] = fSize[i];
            else maxFSize[i] = 8;
        }
        return maxFSize;
    }
}