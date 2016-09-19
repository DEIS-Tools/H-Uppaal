package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.event.EventHandler;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Line;

public class Edge extends Line {

    private Location sourceLocation;
    private Location targetLocation;
    private final MouseTracker mouseTracker;

    public Edge(final Location sourceLocation, final MouseTracker mouseTracker) {
        super(sourceLocation.getCenterX(), sourceLocation.getCenterY(), mouseTracker.getX(), mouseTracker.getY());

        this.sourceLocation = sourceLocation;
        this.mouseTracker = mouseTracker;

        mouseTracker.registerOnMouseMovedEventHandler(mouseMovedEventHandler);
    }

    private final EventHandler<MouseEvent> mouseMovedEventHandler = event -> {
        this.setEndX(event.getX());
        this.setEndY(event.getY());
    };

    public Location getSourceLocation() {
        final Location sourceLocation = this.sourceLocation;
        return sourceLocation;
    }

    public void setSourceLocation(final Location sourceLocation) {
        this.sourceLocation = sourceLocation;
        this.setEndX(sourceLocation.getCenterX());
        this.setEndY(sourceLocation.getCenterY());
    }

    public Location getTargetLocation() {
        final Location targetLocation = this.targetLocation;
        return targetLocation;
    }

    public void setTargetLocation(final Location targetLocation) {
        mouseTracker.unregisterOnMouseMovedEventHandler(mouseMovedEventHandler);

        this.targetLocation = targetLocation;
        this.setEndX(targetLocation.getCenterX());
        this.setEndY(targetLocation.getCenterY());
    }

}
