package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import javafx.event.EventHandler;
import javafx.geometry.Point2D;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
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
        KeyboardTracker.registerKeybind(removeOnEscape);
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        Pane parent = (Pane) this.getParent();
        if(parent == null) return;
        parent.getChildren().remove(this);

        // Notify the canvas that we not longer are creating an edge
        ModelCanvas.edgeOnMouse = null;
    });

    private final EventHandler<MouseEvent> mouseMovedEventHandler = event -> {
        this.setEndX(event.getX() - 2);
        this.setEndY(event.getY() - 2);
        
        double angle = Math.atan2(sourceLocation.getCenterY() - event.getY(), sourceLocation.getCenterX() - event.getX()) - Math.toRadians(180);
        double newX = sourceLocation.getCenterX() + Location.RADIUS * Math.cos(angle);
        double newY = sourceLocation.getCenterY() + Location.RADIUS * Math.sin(angle);

        this.setStartX(newX);
        this.setStartY(newY);
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
        KeyboardTracker.unregisterKeybind(removeOnEscape);

        this.targetLocation = targetLocation;
        this.setEndX(targetLocation.getCenterX());
        this.setEndY(targetLocation.getCenterY());
    }

}
