package dk.cs.aau.huppaal.exceptions;

public class InvalidFormatException extends Exception {
    public InvalidFormatException(String s) { super(s); }
    public InvalidFormatException(String s, Throwable e) { super(s, e); }
    public InvalidFormatException(Throwable e) { super(e); }
}
