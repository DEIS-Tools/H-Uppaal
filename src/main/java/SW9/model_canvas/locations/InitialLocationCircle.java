package SW9.model_canvas.locations;

import SW9.model_canvas.Parent;
import javafx.scene.shape.Circle;

public class InitialLocationCircle extends Parent {

    private Circle innerCircle = new Circle();
    public final static double CIRCLE_RATIO = 0.7;

    public InitialLocationCircle(final Location parentLocation) {
        // Bind the inner circle of the initial location to bound within the circle of the location
        innerCircle.centerXProperty().bind(parentLocation.circle.centerXProperty());
        innerCircle.centerYProperty().bind(parentLocation.circle.centerYProperty());
        innerCircle.radiusProperty().bind(parentLocation.circle.radiusProperty().multiply(CIRCLE_RATIO));
        innerCircle.getStyleClass().add("initial-location-inner-circle");
        addChild(innerCircle);
    }
}
