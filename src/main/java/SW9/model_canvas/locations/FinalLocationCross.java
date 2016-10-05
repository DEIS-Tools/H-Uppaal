package SW9.model_canvas.locations;

import SW9.model_canvas.Parent;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.shape.Line;

public class FinalLocationCross extends Parent {

    private static final double RADIUS_DIVIDER = 2d;

    public FinalLocationCross(final Location parentLocation) {
        // Calculate the diff
        DoubleBinding radiusBinding = parentLocation.circle.radiusProperty().divide(RADIUS_DIVIDER);

        // Bind the the two lines of the final location to be an cross
        Line firstLine = new Line();
        firstLine.startXProperty().bind(parentLocation.circle.centerXProperty().subtract(radiusBinding));
        firstLine.startYProperty().bind(parentLocation.circle.centerYProperty().add(radiusBinding));
        firstLine.endXProperty().bind(parentLocation.circle.centerXProperty().add(radiusBinding));
        firstLine.endYProperty().bind(parentLocation.circle.centerYProperty().subtract(radiusBinding));

        Line secondLine = new Line();
        secondLine.startXProperty().bind(parentLocation.circle.centerXProperty().subtract(radiusBinding));
        secondLine.startYProperty().bind(parentLocation.circle.centerYProperty().subtract(radiusBinding));
        secondLine.endXProperty().bind(parentLocation.circle.centerXProperty().add(radiusBinding));
        secondLine.endYProperty().bind(parentLocation.circle.centerYProperty().add(radiusBinding));

        addChildren(firstLine, secondLine);
    }
}
