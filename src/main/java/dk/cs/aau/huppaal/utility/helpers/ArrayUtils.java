package dk.cs.aau.huppaal.utility.helpers;

public class ArrayUtils {
    // TODO: implement a generic version of this (it's a bit difficult though - because of java)
    public static String[] merge(String[] a, String[] b) {
        int a1 = a.length;
        int b1 = b.length;
        int c1 = a1 + b1;
        var c = new String[c1];
        System.arraycopy(a, 0, c, 0, a1);
        System.arraycopy(b, 0, c, a1, b1);
        return c;
    }
}
