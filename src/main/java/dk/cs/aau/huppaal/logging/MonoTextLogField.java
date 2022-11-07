package dk.cs.aau.huppaal.logging;

import javafx.scene.control.TextField;

public class MonoTextLogField extends TextField {
    public MonoTextLogField(String text) {
        super(text);
        setEditable(false);
        getStyleClass().add("copyable-label");
        getStyleClass().add("body2-mono");
        setStyle("-fx-text-fill: white");
    }
}
