package dk.cs.aau.huppaal.logging;

import dk.cs.aau.huppaal.presentations.logging.TextStyle;
import javafx.scene.paint.Color;

public enum LogRegexQuantifiers {
    // CASE/Link Color
    LOCATION("#ffa86c"),
    COMPONENT("#766CFF"),
    FILE("#C9FF6C");

    private final TextStyle style;
    LogRegexQuantifiers() {
        this.style = TextStyle.EMPTY;
    }
    LogRegexQuantifiers(String webColor) {
        this.style = TextStyle.textColor(Color.web(webColor));
    }

    public TextStyle getStyle() {
        return style;
    }
}
