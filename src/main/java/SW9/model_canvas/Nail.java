package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.beans.property.DoubleProperty;
import javafx.scene.shape.Circle;

public class Nail extends Circle {

    private final static double RADIUS = 10d;

    public Nail(final MouseTracker canvasMouseTracker) {
        super(canvasMouseTracker.getXProperty().get(), canvasMouseTracker.getXProperty().get(), RADIUS);
        this.centerXProperty().bind(canvasMouseTracker.getXProperty());
        this.centerYProperty().bind(canvasMouseTracker.getYProperty());
    }
}