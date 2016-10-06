package SW9.model_canvas.edges;

import javafx.beans.binding.DoubleBinding;
import javafx.scene.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;

public class Link extends Parent {

    public final Line line;
    private static final double DRAGGABLE_LINE_STROKE_WIDTH = 13d;

    public Link() {
        // Create the visible line
        line = new Line();
        line.getStyleClass().add("link");

        // Create the draggable line
        Line draggableLine = new Line();
        draggableLine.setStrokeWidth(DRAGGABLE_LINE_STROKE_WIDTH);
        draggableLine.setStroke(Color.TRANSPARENT);

        // Bind the draggable line to the visible line
        draggableLine.mouseTransparentProperty().bind(this.mouseTransparentProperty());
        draggableLine.startXProperty().bind(line.startXProperty());
        draggableLine.startYProperty().bind(line.startYProperty());
        draggableLine.endXProperty().bind(line.endXProperty());
        draggableLine.endYProperty().bind(line.endYProperty());

        this.getChildren().add(line);
        this.getChildren().add(draggableLine);
    }
}
