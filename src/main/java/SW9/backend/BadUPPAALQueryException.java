package SW9.backend;

public class BadUPPAALQueryException extends Exception {
    public BadUPPAALQueryException(final String s) {
        super(s);
    }

    public BadUPPAALQueryException(final String s, final Exception cause) {
        super(s, cause);
    }
}
