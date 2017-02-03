package SW9.abstractions;

import SW9.utility.colors.Color;

public enum QueryState {
    SUCCESSFUL(Color.GREEN, Color.Intensity.I700),
    ERROR(Color.RED, Color.Intensity.I700),
    RUNNING(Color.GREY_BLUE, Color.Intensity.I600),
    UNKNOWN(Color.GREY, Color.Intensity.I600),
    SYNTAX_ERROR(Color.PURPLE, Color.Intensity.I700);

    private final Color color;
    private final Color.Intensity colorIntensity;

    QueryState(final Color color, final Color.Intensity colorIntensity) {
        this.color = color;
        this.colorIntensity = colorIntensity;
    }

    public Color getColor() {
        return color;
    }

    public Color.Intensity getColorIntensity() {
        return colorIntensity;
    }
}
