package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.utility.DragHelper;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Location extends Circle implements MouseTracker.hasMouseTracker {

    // Used to create the Location
    public final static double RADIUS = 25.0f;

    // Used to drag the location around
    private double dragXOffSet = 0;
    private double dragYOffSet = 0;
    private boolean isBeingDragged = false;

    // Used to update the interaction with the mouse
    private boolean isOnMouse = true;
    public final MouseTracker localMouseTracker;

    public Location(MouseTracker canvasMouseTracker) {
        this(canvasMouseTracker.getXProperty(), canvasMouseTracker.getYProperty(), canvasMouseTracker);
    }

    public Location(final DoubleProperty centerX, final DoubleProperty centerY, final MouseTracker canvasMouseTracker) {
        super(centerX.doubleValue(), centerY.doubleValue(), RADIUS);

        // Bind the Location to the property
        this.centerXProperty().bind(centerX);
        this.centerYProperty().bind(centerY);

        // Initialize the local mouse tracker
        this.localMouseTracker = new MouseTracker(this);

        // Add style
        this.getStyleClass().add("location");

        // Register the handler for entering the location
        localMouseTracker.registerOnMouseEnteredEventHandler(event -> {
            ModelCanvas.setHoveredLocation(this);
        });

        // Register the handler for existing the locations
        localMouseTracker.registerOnMouseExitedEventHandler(event -> {
            if(ModelCanvas.mouseIsHoveringLocation() && ModelCanvas.getHoveredLocation().equals(this)) {
                ModelCanvas.setHoveredLocation(null);
            }
        });

        // Place the new location when the mouse is pressed (i.e. stop moving it)
        localMouseTracker.registerOnMousePressedEventHandler( mouseClickedEvent -> {
            if (isOnMouse) {
                this.centerXProperty().unbind();
                this.centerYProperty().unbind();

                // Tell the canvas that the mouse is no longer occupied
                ModelCanvas.setLocationOnMouse(null);

                // Disable discarding functionality for a location when it is placed on the canvas
                KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_LOCATION);

                // Animate the way the location is placed on the canvas
                Animation locationPlaceAnimation = new Transition() {
                    {
                        setCycleDuration(Duration.millis(50));
                    }

                    protected void interpolate(double frac) {
                        Location.this.setEffect(DropShadowHelper.generateElevationShadow(12 - 12 * frac));
                    }
                };

                // When the animation is done ensure that we note that we are no longer on the mouse
                locationPlaceAnimation.setOnFinished(event -> {
                    isOnMouse = false;
                });

                locationPlaceAnimation.play();
            }
        });

        // Make the location draggable
        DragHelper.makeDraggable(this, (event) -> !event.isShiftDown());

        // Draw a new edge from the location
        localMouseTracker.registerOnMousePressedEventHandler(event -> {
            if(event.isShiftDown()) {
                final Edge edge = new Edge(this, canvasMouseTracker);
                addChildToParent(edge);
            }
        });

        // Add keybind to discard the location if ESC is pressed
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_LOCATION, removeOnEscape);
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        // Get the parent and detach this Location
        Pane parent = (Pane) this.getParent();
        if(parent == null) return;
        parent.getChildren().remove(this);

        // Notify the canvas that we are no longer placing a location
        ModelCanvas.setLocationOnMouse(null);
    });

    private void addChildToParent(final Node node) {
        // Get the parent from the source location
        Pane parent = (Pane) this.getParent();

        if (parent == null) return;

        parent.getChildren().add(node);
    }


    @Override
    public MouseTracker getMouseTracker() {
        return this.localMouseTracker;
    }

    @Override
    public DoubleProperty xProperty() {
        return this.centerXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return this.centerYProperty();
    }
}
