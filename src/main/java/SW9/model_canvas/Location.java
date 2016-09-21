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

    // Used to drag the location around
    private double dragXoffSet = 0;
    private double dragYoffSet = 0;
    private boolean isBeingDragged = false;

    public final static double RADIUS = 25.0f;

    private boolean isOnMouse = true;
    public final MouseTracker localMouseTracker;

    public Location(MouseTracker parentMouseTracker) {
        this(parentMouseTracker.getXProperty(), parentMouseTracker.getYProperty(), parentMouseTracker);
    }

    public Location(final DoubleProperty centerX, final DoubleProperty centerY, final MouseTracker parentMouseTracker) {
        super(centerX.doubleValue(), centerY.doubleValue(), RADIUS);
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
            if(ModelCanvas.locationIsHovered() && ModelCanvas.getHoveredLocation().equals(this)) {
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
            }
        });

        // Enable dragging of locations
        localMouseTracker.registerOnMousePressedEventHandler(event -> {
            if(!event.isShiftDown()) {
                dragXoffSet = this.centerXProperty().get() - event.getX();
                dragYoffSet = this.centerYProperty().get() - event.getY();
                isBeingDragged = true;
            }
        });

        // Make the location follow the mouse on drag (if enabled)
        localMouseTracker.registerOnMouseDraggedEventHandler(event -> {
            if(isBeingDragged) {
                this.setCenterX(event.getX() + dragXoffSet);
                this.setCenterY(event.getY() + dragYoffSet);
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
                System.out.println("TODO tegn en edge");
            }
        });

        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_LOCATION, removeOnEscape);
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        Pane parent = (Pane) this.getParent();
        if(parent == null) return;
        parent.getChildren().remove(this);

        // Notify the canvas that we are no longer placing a location
        ModelCanvas.setLocationOnMouse(null);
    });


}
