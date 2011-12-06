package nl.pwy.pdxreader;

public class Misc {
    /**
     * Check if char is plain ASCII upper case.
     *
     * @param c char to check.
     * @return true if char is in range A..Z.
     * @see Character#isUpperCase
     */
    public static boolean isUnaccentedUpperCase(char c) {
        return 'A' <= c && c <= 'Z';
    } // end isUnaccentedUpperCase

    /**
     * Check if char is plain ASCII lower case.
     *
     * @param c char to check
     * @return true if char is in range a..z
     * @see Character#isLowerCase
     */
    public static boolean isUnaccentedLowerCase(char c) {
        return 'a' <= c && c <= 'z';
    } // isUnaccentedLowerCase

    /**
     * Is this string empty?
     *
     * @param s String to be tested for emptiness.
     * @return true if the string is null or equal to the "" null string.
     *         or just blanks
     */
    public static boolean isEmpty(String s) {
        return (s == null) || s.trim().length() == 0;
    } // end isEmpty

    /**
     * Convert String to canonical standard form.
     * null -> "".
     * Trims lead trail blanks.
     *
     * @param s String to be converted.
     * @return String in canonical form.
     */
    public static String canonical(String s) {
        if (s == null) return "";
        else return s.trim();
    } // end canonical

    /**
     * Produce a String of a given repeating character.
     *
     * @param c     the character to repeat
     * @param count the number of times to repeat
     * @return String, e.g. rep('*',4) returns "****"
     */
    public static String rep(char c, int count) {
        // This code is infuriatingly inefficient.
        // The StringBuffer array gets cleared twice,
        // once by the StringBuffer constructor and
        // once by setLength. Neither clearing is needed.
        StringBuilder s = new StringBuilder(count);
        s.setLength(count);
        for (int i = 0; i < count; i++) {
            s.setCharAt(i, c);
        }
        return s.toString().intern();
    } // end rep

    /**
     * Convert an integer to a String, with left zeroes.
     *
     * @param i   the integer to be converted
     * @param len the length of the resulting string
     * @return String representation of the int e.g. 007
     */
    public static String toLZ(int i, int len) {
        // Since String is final, we could not add this method there.
        String s = Integer.toString(i);
        if (s.length() > len) return s.substring(0, len);
        else if (s.length() < len)
            // pad on left with zeros
            return "000000000000000000000000000".substring(0, len - s.length()) + s;
        else return s;
    } // end toLZ

    /**
     * Extracts a number from a string, returns 0 if malformed.
     *
     * @param s String containing the integer.
     * @return binary integer.
     */
    public static int pluck(String s) {
        int result = 0;
        try {
            result = Integer.parseInt(s);
        } catch (NumberFormatException ignored) {

        }
        return result;
    } //end pluck

    /**
     * Caps the max value, ensuring it does not go too high.
     * alias for min.
     *
     * @param v    the value
     * @param high the high bound above which v cannot go.
     * @return the lesser of v and high.
     * @see Math#min
     */
    public static int cap(int v, int high) {
        if (v > high) return high;
        else return v;
    } // end cap

    /**
     * Ensures a value does not go too low.
     * alias for max
     *
     * @param v   the value
     * @param low the low bound below which v cannot go.
     * @return the greater of v and low.
     * @see Math#max
     */
    public static int hem(int v, int low) {
        if (v < low) return low;
        else return v;
    } // end hem

    /**
     * Corrals a value back into safe bounds.
     *
     * @param v    the value
     * @param low  the low bound below which v cannot go.
     * @param high the high bound above which v cannot go.
     * @return low if v < low, high if v > high, but normally just v.
     */
    public static int corral(int v, int low, int high) {
        if (v < low) return low;
        else if (v > high) return high;
        else return v;
    } // end corral

    /**
     * makeshift system beep if awt.Toolkit.beep is not available.
     * Works also in JDK 1.02.
     */
    public static void beep() {
        System.out.print("\007");
        System.out.flush();
    } // end beep
}
