package nl.pwy.pdxreader;

import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public class LEDataInputStream implements DataInput {
    /**
     * constructor
     *
     * @param in
     */
    public LEDataInputStream(InputStream in) {
        this.in = in;
        this.d = new DataInputStream(in);
        w = new byte[8];
    }

    // L I T T L E   E N D I A N   R E A D E R S
    // Little endian methods for multi-byte numeric types.
    // Big-endian do fine for single-byte types and strings.

    /**
     * like DataInputStream.readShort except little endian.
     */
    public final short readShort() throws IOException {
        d.readFully(w, 0, 2);
        return (short) (
                (w[1] & 0xff) << 8 |
                        (w[0] & 0xff));
    }

    /**
     * like DataInputStream.readUnsignedShort except little endian.
     * Note, returns int even though it reads a short.
     */
    public final int readUnsignedShort() throws IOException {
        d.readFully(w, 0, 2);
        return (
                (w[1] & 0xff) << 8 |
                        (w[0] & 0xff));
    }

    /**
     * like DataInputStream.readChar except little endian.
     */
    public final char readChar() throws IOException {
        d.readFully(w, 0, 2);
        return (char) (
                (w[1] & 0xff) << 8 |
                        (w[0] & 0xff));
    }

    /**
     * like DataInputStream.readInt except little endian.
     */
    public final int readInt() throws IOException {
        d.readFully(w, 0, 4);
        return
                (w[3] & 0xff) << 24 |
                        (w[2] & 0xff) << 16 |
                        (w[1] & 0xff) << 8 |
                        (w[0] & 0xff);
    }

    /**
     * like DataInputStream.readLong except little endian.
     */
    public final long readLong() throws IOException {
        d.readFully(w, 0, 8);
        return
                (long) (w[7] & 0xff) << 56 |
                        (long) (w[6] & 0xff) << 48 |
                        (long) (w[5] & 0xff) << 40 |
                        (long) (w[4] & 0xff) << 32 |
                        (long) (w[3] & 0xff) << 24 |
                        (long) (w[2] & 0xff) << 16 |
                        (long) (w[1] & 0xff) << 8 |
                        (long) (w[0] & 0xff);
    }

    /**
     * like DataInputStream.readFloat except little endian.
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * like DataInputStream.readDouble except little endian.
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }

    // p u r e l y   w r a p p e r   m e t h o d s
    // We can't simply inherit since dataInputStream is final.

    /* Watch out, may return fewer bytes than requested. */
    public final int read(byte b[], int off, int len) throws IOException {
        // For efficiency, we avoid one layer of wrapper
        return in.read(b, off, len);
    }

    public final void readFully(byte b[]) throws IOException {
        d.readFully(b, 0, b.length);
    }

    public final void readFully(byte b[], int off, int len) throws IOException {
        d.readFully(b, off, len);
    }

    public final int skipBytes(int n) throws IOException {
        return d.skipBytes(n);
    }

    /* only reads one byte */
    public final boolean readBoolean() throws IOException {
        return d.readBoolean();
    }

    public final byte readByte() throws IOException {
        return d.readByte();
    }

    // note: returns an int, even though says Byte.
    public final int readUnsignedByte() throws IOException {
        return d.readUnsignedByte();
    }

    @Deprecated
    public final String readLine() throws IOException {
        return d.readLine();
    }

    public final String readUTF() throws IOException {
        return d.readUTF();
    }

    // Note. This is a STATIC method!
    public static String readUTF(DataInput in) throws IOException {
        return DataInputStream.readUTF(in);
    }

    public final void close() throws IOException {
        d.close();
    }

    private final DataInputStream d; // to get at high level readFully methods of DataInputStream
    private final InputStream in;    // to get at the low-level read methods of InputStream
    private final byte[] w; // work array for buffering input

}
