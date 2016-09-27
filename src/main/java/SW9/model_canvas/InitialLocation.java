package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.Circle;

public class InitialLocation extends Location {

    private Circle innerCircle = new Circle();
    public final static double CIRCLE_RATIO = 0.7;

    public InitialLocation(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY) {
        super(centerX,centerY, null);
        // Bind the inner circle of the initial location to bound within the circle of the location
        innerCircle.centerXProperty().bind(circle.centerXProperty());
        innerCircle.centerYProperty().bind(circle.centerYProperty());
        innerCircle.radiusProperty().bind(circle.radiusProperty().multiply(CIRCLE_RATIO));
        innerCircle.getStyleClass().add("initial-location-inner-circle");
        getChildren().add(innerCircle);
    }
}
