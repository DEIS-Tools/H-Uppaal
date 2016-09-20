package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import javafx.event.EventHandler;
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
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_EDGE, removeOnEscape);
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        Pane parent = (Pane) this.getParent();
        if (parent == null) return;
        parent.getChildren().remove(this);

        // Notify the canvas that we not longer are creating an edge
        ModelCanvas.edgeOnMouse = null;
    });

    private final EventHandler<MouseEvent> mouseMovedEventHandler = event -> {
        double angle,
                newX,
                newY;


        if (ModelCanvas.locationIsHovered()) {
            angle = Math.atan2(sourceLocation.getCenterY() - ModelCanvas.hoveredLocation.getCenterY(), sourceLocation.getCenterX() - ModelCanvas.hoveredLocation.getCenterX());
            newX = ModelCanvas.hoveredLocation.getCenterX() + Location.RADIUS * Math.cos(angle);
            newY = ModelCanvas.hoveredLocation.getCenterY() + Location.RADIUS * Math.sin(angle);

            this.setEndX(newX);
            this.setEndY(newY);
            angle -= Math.toRadians(180);
            newX = sourceLocation.getCenterX() + Location.RADIUS * Math.cos(angle) ;
            newY = sourceLocation.getCenterY() + Location.RADIUS * Math.sin(angle);

            this.setStartX(newX);
            this.setStartY(newY);

        } else {
            this.setEndX(event.getX() - 1);
            this.setEndY(event.getY() - 1);

            angle = Math.atan2(sourceLocation.getCenterY() - event.getY(), sourceLocation.getCenterX() - event.getX()) - Math.toRadians(180);
            newX = sourceLocation.getCenterX() + Location.RADIUS * Math.cos(angle);
            newY = sourceLocation.getCenterY() + Location.RADIUS * Math.sin(angle);

            this.setStartX(newX);
            this.setStartY(newY);
        }

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
        KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_EDGE);

        double angle = Math.atan2(sourceLocation.getCenterY() - targetLocation.getCenterY(), sourceLocation.getCenterX() - targetLocation.getCenterX());
        double newX = targetLocation.getCenterX() + Location.RADIUS * Math.cos(angle);
        double newY = targetLocation.getCenterY() + Location.RADIUS * Math.sin(angle);

        this.setEndX(newX);
        this.setEndY(newY);

        this.targetLocation = targetLocation;
    }

}
