package dk.cs.aau.huppaal.logging;

import dk.cs.aau.huppaal.presentations.logging.TextStyle;
import javafx.scene.paint.Color;

public enum LogLinkQuantifier {
    // CASE/Link Color
    LOCATION("#ffa86c"),
    COMPONENT("#C9FF6C"),
    FILE("#766CFF"),
    UNKNOWN;

    private final TextStyle style;
    LogLinkQuantifier() {
        this.style = TextStyle.EMPTY;
    }
    LogLinkQuantifier(String webColor) {
        this.style = TextStyle.textColor(Color.web(webColor));
    }

    public TextStyle getStyle() {
        return style;
    }
}
