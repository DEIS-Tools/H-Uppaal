package dk.cs.aau.huppaal.logging;

import dk.cs.aau.huppaal.presentations.logging.TextStyle;
import dk.cs.aau.huppaal.utility.colors.Color;

public enum LogLinkQuantifier {
    // CASE/Link Color
    COMPONENT(Color.PURPLE),
    GENERIC(Color.BLUE),
    LOCATION(Color.YELLOW),
    SUBCOMPONENT(Color.YELLOW),
    JORK(Color.YELLOW),
    EDGE(Color.CYAN),
    NAIL(Color.CYAN),
    UNKNOWN(Color.GREY);

    private final TextStyle style;

    LogLinkQuantifier() {
        this.style = TextStyle.EMPTY;
    }
    LogLinkQuantifier(Color color) {
        this.style = TextStyle.textColor(color.getColor(Color.Intensity.I500));
    }

    public TextStyle getStyle() {
        return style;
    }
}
