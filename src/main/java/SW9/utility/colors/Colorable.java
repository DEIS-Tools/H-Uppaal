package SW9.utility.colors;

public interface Colorable {

    boolean isColored();

    Color getColor();

    Color.Intensity getIntensity();

    void color(final Color color, final Color.Intensity intensity);

    void resetColor();

}
