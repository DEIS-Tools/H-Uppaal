package SW9.utility.colors;

import javafx.scene.input.KeyCode;

import java.util.ArrayList;

public class EnabledColor {

    public static final ArrayList<EnabledColor> enabledColors = new ArrayList<EnabledColor>() {{
        add(new EnabledColor(Color.GREY_BLUE, Color.Intensity.I700, KeyCode.DIGIT0));
        add(new EnabledColor(Color.DEEP_ORANGE, Color.Intensity.I700, KeyCode.DIGIT1));
        add(new EnabledColor(Color.RED, Color.Intensity.I700, KeyCode.DIGIT2));
        add(new EnabledColor(Color.PINK, Color.Intensity.I500, KeyCode.DIGIT3));
        add(new EnabledColor(Color.PURPLE, Color.Intensity.I500, KeyCode.DIGIT4));
        add(new EnabledColor(Color.INDIGO, Color.Intensity.I500, KeyCode.DIGIT5));
        add(new EnabledColor(Color.BLUE, Color.Intensity.I600, KeyCode.DIGIT6));
        add(new EnabledColor(Color.CYAN, Color.Intensity.I700, KeyCode.DIGIT7));
        add(new EnabledColor(Color.GREEN, Color.Intensity.I600, KeyCode.DIGIT8));
        add(new EnabledColor(Color.BROWN, Color.Intensity.I500, KeyCode.DIGIT9));
    }};

    public final Color color;
    public final Color.Intensity intensity;
    public final KeyCode keyCode;

    public EnabledColor(final Color color, final Color.Intensity intensity) {
        this(color, intensity, null);
    }

    public EnabledColor(final Color color, final Color.Intensity intensity, final KeyCode keyCode) {
        this.color = color;
        this.intensity = intensity;
        this.keyCode = keyCode;
    }

    public static String getIdentifier(final Color color) {
        for (final EnabledColor enabledColor : enabledColors) {
            if (enabledColor.color.equals(color)) {
                return enabledColor.keyCode.getName();
            }
        }

        return "";
    }

    public static EnabledColor fromIdentifier(final String identifier) {
        for (final EnabledColor enabledColor : enabledColors) {
            if (enabledColor.keyCode.getName().equals(identifier)) {
                return enabledColor;
            }
        }

        return null;
    }

    @Override
    public boolean equals(final Object obj) {
        return obj instanceof EnabledColor && ((EnabledColor) obj).color.equals(this.color);
    }
}