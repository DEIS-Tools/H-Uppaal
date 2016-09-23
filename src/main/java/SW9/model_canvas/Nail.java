package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Nail extends Circle {

    private final static double RADIUS = 5d;

    public Nail(final double centerX, final double centerY) {
        super(centerX, centerY, RADIUS);
        this.setFill(Color.grayRgb(100, 0.5));
    }
}