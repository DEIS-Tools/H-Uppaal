package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.beans.binding.Bindings;
import javafx.scene.shape.Circle;

public class InitialLocation extends Location {

    private Circle innerCircle = new Circle();
    public final static double CIRCLE_DIFF = 7d;

    public InitialLocation(MouseTracker canvasMouseTracker) {
        super(canvasMouseTracker);

        // Bind the inner circle of the initial location to bound within the circle of the location
        innerCircle.centerXProperty().bind(circle.centerXProperty());
        innerCircle.centerYProperty().bind(circle.centerYProperty());
        innerCircle.radiusProperty().bind(Bindings.subtract(circle.radiusProperty(), CIRCLE_DIFF));
        innerCircle.getStyleClass().add("initial-location-inner-circle");
        getChildren().add(innerCircle);
    }
}
