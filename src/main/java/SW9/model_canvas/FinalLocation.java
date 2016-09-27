package SW9.model_canvas;

import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.Line;

public class FinalLocation extends Location {

    private Line firstLine = new Line();
    private Line secondLine = new Line();

    private static final double RADIUS_DIVIDER = 2d;

    public FinalLocation(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY) {
        super(centerX, centerY, null);

        // Calculate the diff
        DoubleBinding radiusBinding = circle.radiusProperty().divide(RADIUS_DIVIDER);

        // Bind the the two lines of the final location to be an cross
        firstLine.startXProperty().bind(circle.centerXProperty().subtract(radiusBinding));
        firstLine.startYProperty().bind(circle.centerYProperty().add(radiusBinding));
        firstLine.endXProperty().bind(circle.centerXProperty().add(radiusBinding));
        firstLine.endYProperty().bind(circle.centerYProperty().subtract(radiusBinding));

        secondLine.startXProperty().bind(circle.centerXProperty().subtract(radiusBinding));
        secondLine.startYProperty().bind(circle.centerYProperty().subtract(radiusBinding));
        secondLine.endXProperty().bind(circle.centerXProperty().add(radiusBinding));
        secondLine.endYProperty().bind(circle.centerYProperty().add(radiusBinding));

        getChildren().add(firstLine);
        getChildren().add(secondLine);


    }
}
