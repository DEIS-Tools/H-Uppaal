package model_canvas;

import javafx.scene.shape.Circle;

public class Location extends Circle {

    private final static double RADIUS = 25.0f;

    public Location() {
        super(RADIUS);

        this.getStyleClass().add("location");
    }

    public Location(final double centerX, final double centerY) {
        super(centerX, centerY, RADIUS);

        this.getStyleClass().add("location");
    }

}
