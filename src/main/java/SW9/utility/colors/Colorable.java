package SW9.utility.colors;

public interface Colorable {

    boolean isColored();

    void color(final Color color, final Color.Intensity intensity);

    void resetColor();

}
