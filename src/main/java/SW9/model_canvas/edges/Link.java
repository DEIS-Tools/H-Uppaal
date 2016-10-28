package SW9.model_canvas.edges;

import SW9.utility.UndoRedoStack;
import SW9.utility.mouse.MouseTracker;
import javafx.scene.Parent;
import javafx.scene.shape.Line;

public class Link extends Parent {

    public final Line line;
    private static final double DRAGGABLE_LINE_STROKE_WIDTH = 13d;
    private MouseTracker mouseTracker = new MouseTracker(this);

    public Link() {
        // Create the visible line
        line = new Line();
        line.getStyleClass().add("link");

        // Create the draggable line
        Line draggableLine = new Line();
        draggableLine.setStrokeWidth(DRAGGABLE_LINE_STROKE_WIDTH);
        draggableLine.setOpacity(0);

        // Bind the draggable line to the visible line
        draggableLine.mouseTransparentProperty().bind(this.mouseTransparentProperty());
        draggableLine.startXProperty().bind(line.startXProperty());
        draggableLine.startYProperty().bind(line.startYProperty());
        draggableLine.endXProperty().bind(line.endXProperty());
        draggableLine.endYProperty().bind(line.endYProperty());

        this.getChildren().add(line);
        this.getChildren().add(draggableLine);

        mouseTracker.registerOnMousePressedEventHandler((event -> {

            if (event.isShiftDown()) {
                final Edge edgeParent = getEdgeParent();
                final int myPosition = edgeParent.links.indexOf(this);
                final MouseTracker edgeMousetracker = edgeParent.getMouseTracker();
                final Nail newNail = new Nail(
                        edgeParent.xProperty().add(edgeMousetracker.xProperty().get() - edgeParent.xProperty().get()),
                        edgeParent.yProperty().add(edgeMousetracker.yProperty().get() - edgeParent.yProperty().get())
                );

                UndoRedoStack.push(
                        () -> edgeParent.add(newNail, myPosition),
                        () -> edgeParent.remove(newNail)
                );
            }
        }));
    }

    private Edge getEdgeParent() {
        Parent parent = getParent();
        while (parent != null) {
            if (parent instanceof Edge) {
                return ((Edge) parent);

            }
            parent = parent.getParent();
        }
        return null;
    }
}
