package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Location extends Circle {

    // Used to create the Location
    private final static double RADIUS = 25.0f;

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
            if(ModelCanvas.mouseHoveringLocation() && ModelCanvas.getHoveredLocation().equals(this)) {
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

        // Enable dragging of locations (only if shift is not used)
        localMouseTracker.registerOnMousePressedEventHandler(event -> {
            if(!event.isShiftDown()) {
                dragXOffSet = this.centerXProperty().get() - event.getX();
                dragYOffSet = this.centerYProperty().get() - event.getY();
                isBeingDragged = true;
            }
        });

        // Make the location follow the mouse on drag (if enabled)
        localMouseTracker.registerOnMouseDraggedEventHandler(event -> {
            if(isBeingDragged) {
                this.setCenterX(event.getX() + dragXOffSet);
                this.setCenterY(event.getY() + dragYOffSet);
            }
        });

        // Disable dragging of location
        localMouseTracker.registerOnMouseReleasedEventHandler(event -> {
           if(isBeingDragged) {
              isBeingDragged = false;
           }
        });

        // Draw a new edge from the location
        localMouseTracker.registerOnMousePressedEventHandler(event -> {
            if(event.isShiftDown()) {
                // TODO draw an edge
                System.out.println("TODO tegn en edge");
                new Edge(this, canvasMouseTracker);
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


}
