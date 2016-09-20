package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Location extends Circle {

    public final static double RADIUS = 25.0f;

    private boolean isOnMouse = true;
    public final MouseTracker localMouseTracker = new MouseTracker();

    public Location(MouseTracker parentMouseTracker) {
        this(parentMouseTracker.getXProperty(), parentMouseTracker.getYProperty(), parentMouseTracker);
    }

    public Location(final DoubleProperty centerX, final DoubleProperty centerY, final MouseTracker parentMouseTracker) {
        super(centerX.doubleValue(), centerY.doubleValue(), RADIUS);
        this.centerXProperty().bind(centerX);
        this.centerYProperty().bind(centerY);

        // Initialize the local mouse tracker
        this.setOnMouseMoved(localMouseTracker.onMouseMovedEventHandler);
        this.setOnMouseClicked(localMouseTracker.onMouseClickedEventHandler);
        this.setOnMouseEntered(localMouseTracker.onMouseEnteredEventHandler);
        this.setOnMouseExited(localMouseTracker.onMouseExitedEventHandler);
        this.setOnMouseDragged(localMouseTracker.onMouseDraggesEventHandler);

        // Add style
        this.getStyleClass().add("location");


        final EventHandler<MouseEvent> mouseEntered = event -> {
            ModelCanvas.hoveredLocation = this;
        };

        final EventHandler<MouseEvent> mouseDragged = event -> {
            ModelCanvas.locationOnMouse = this;
            this.centerXProperty().bind(parentMouseTracker.getXProperty());
            this.centerYProperty().bind(parentMouseTracker.getYProperty());
            isOnMouse = true;
        };

        final EventHandler<MouseEvent> mouseExited = event -> {
            if(ModelCanvas.hoveredLocation == this) {
                ModelCanvas.hoveredLocation = null;
            }
        };

        // Place the new location when the mouse is pressed (i.e. stop moving it)
        final EventHandler<MouseEvent> locationMouseClick = mouseClickedEvent -> {
            if (isOnMouse) {
                this.centerXProperty().unbind();
                this.centerYProperty().unbind();

                // Tell the canvas that the mouse is no longer occupied
                ModelCanvas.locationOnMouse = null;
                KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_LOCATION);


                Animation locationPlaceAnimation = new Transition() {
                    {
                        setCycleDuration(Duration.millis(50));
                    }

                    protected void interpolate(double frac) {
                        Location.this.setEffect(DropShadowHelper.generateElevationShadow(12 - 12 * frac));
                    }
                };

                locationPlaceAnimation.play();

                locationPlaceAnimation.setOnFinished(event -> {
                    isOnMouse = false;
                });
            } else if (mouseClickedEvent.isShiftDown() && !ModelCanvas.mouseHasEdge()) {

                final Edge edge = new Edge(this, parentMouseTracker);

                // Type cast the parent to be the anchor pane and disregard the safety and simple add the edge
                //((Pane) this.getParent()).getChildren().add(edge);

                // Notify the canvas that we are creating an edge
                ModelCanvas.edgeOnMouse = edge;


            } else if (ModelCanvas.mouseHasEdge()) {
                ModelCanvas.edgeOnMouse.setTargetLocation(this);
                ModelCanvas.edgeOnMouse = null;
            }
        };


        // Register the handler for placing the location
        localMouseTracker.registerOnMouseClickedEventHandler(locationMouseClick);

        // Register the handler for dragging of the location (is unregistered when clicked)
        localMouseTracker.registerOnMouseEnteredEventHandler(mouseEntered);
        localMouseTracker.registerOnMouseExitedEventHandler(mouseExited);
        localMouseTracker.registerOnMouseDraggedEventHandler(mouseDragged);

        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_LOCATION, removeOnEscape);
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        Pane parent = (Pane) this.getParent();
        if(parent == null) return;
        parent.getChildren().remove(this);

        // Notify the canvas that we not longer are creating an edge
        ModelCanvas.locationOnMouse = null;
    });


}
