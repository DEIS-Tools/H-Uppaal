package dk.cs.aau.huppaal.logging;


import javafx.scene.control.Label;

public class MonoTextLabel extends Label {
    public MonoTextLabel(String text) {
        super(text);
        getStyleClass().add("body2-mono");
        setStyle("-fx-text-fill: white");
    }
}
