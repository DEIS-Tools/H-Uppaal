package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.shape.Line;

public class FinalLocation extends Location {

    private Line firstLine = new Line();
    private Line secondLine = new Line();

    public FinalLocation(MouseTracker canvasMouseTracker) {
        super(canvasMouseTracker);

        // Calculate the diff
        DoubleBinding radiusBinding = Bindings.divide(circle.radiusProperty(), 2d);

        // Bind the the two lines of the final location to be an cross
        firstLine.startXProperty().bind(Bindings.subtract(circle.centerXProperty(), radiusBinding));
        firstLine.startYProperty().bind(Bindings.add(circle.centerYProperty(), radiusBinding));
        firstLine.endXProperty().bind(Bindings.add(circle.centerXProperty(), radiusBinding));
        firstLine.endYProperty().bind(Bindings.subtract(circle.centerYProperty(), radiusBinding));

        secondLine.startXProperty().bind(Bindings.subtract(circle.centerXProperty(), radiusBinding));
        secondLine.startYProperty().bind(Bindings.subtract(circle.centerYProperty(), radiusBinding));
        secondLine.endXProperty().bind(Bindings.add(circle.centerXProperty(), radiusBinding));
        secondLine.endYProperty().bind(Bindings.add(circle.centerYProperty(), radiusBinding));

        getChildren().add(firstLine);
        getChildren().add(secondLine);


    }
}
