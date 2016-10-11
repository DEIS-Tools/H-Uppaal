package SW9.utility.colors;

public interface Colorable {

    boolean isColored();

    Color getColor();

    Color.Intensity getColorIntensity();

    boolean color(final Color color, final Color.Intensity intensity);

    void resetColor();

    void resetColor(final Color color, final Color.Intensity intensity);

}
