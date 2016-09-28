package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.utility.BindingHelper;
import SW9.utility.DragHelper;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

public class Location extends Parent implements MouseTracker.hasMouseTracker {

    // Used to create the Location
    public final static double RADIUS = 25.0f;

    // Used to update the interaction with the mouse
    private boolean isOnMouse = true;
    public final MouseTracker localMouseTracker;

    public Circle circle;
    public Label locationLabel;

    public BooleanProperty isUrgent = new SimpleBooleanProperty(false);
    public BooleanProperty isCommitted = new SimpleBooleanProperty(false);

    public Location(MouseTracker canvasMouseTracker) {
        this(canvasMouseTracker.getXProperty(), canvasMouseTracker.getYProperty(), canvasMouseTracker);
    }

    public Location(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY, final MouseTracker canvasMouseTracker) {
        // Add the circle and add it at a child
        circle = new Circle(centerX.doubleValue(), centerY.doubleValue(), RADIUS);
        addChild(circle);

        // Add a text which we will use as a label for urgent and committed locations
        locationLabel = new Label();
        locationLabel.textProperty().bind(new StringBinding() {
            {
                super.bind(isUrgent, isCommitted);
            }

            @Override
            protected String computeValue() {
                if (isUrgent.get()) {
                    return "U";
                } else if (isCommitted.get()) {
                    return "C";
                } else {
                    return "";
                }
            }
        });
        locationLabel.setMinWidth(2 * RADIUS);
        locationLabel.setMaxWidth(2 * RADIUS);
        locationLabel.setMinHeight(2 * RADIUS);
        locationLabel.setMaxHeight(2 * RADIUS);
        addChild(locationLabel);

        // Bind the label to the circle
        BindingHelper.bind(locationLabel, this);

        // Add style for the circle and label
        circle.getStyleClass().add("location");
        locationLabel.getStyleClass().add("location-label");
        locationLabel.getStyleClass().add("headline");
        locationLabel.getStyleClass().add("white-text");

        // Bind the Location to the property
        circle.centerXProperty().bind(centerX);
        circle.centerYProperty().bind(centerY);

        // Initialize the local mouse tracker
        this.localMouseTracker = new MouseTracker(this);

        // Register the handler for entering the location
        localMouseTracker.registerOnMouseEnteredEventHandler(event -> {
            ModelCanvas.setHoveredLocation(this);
        });

        // Register the handler for existing the locations
        localMouseTracker.registerOnMouseExitedEventHandler(event -> {
            if (ModelCanvas.mouseIsHoveringLocation() && ModelCanvas.getHoveredLocation().equals(this)) {
                ModelCanvas.setHoveredLocation(null);
            }
        });

        // Place the new location when the mouse is pressed (i.e. stop moving it)
        localMouseTracker.registerOnMousePressedEventHandler(mouseClickedEvent -> {
            if (isOnMouse) {
                circle.centerXProperty().unbind();
                circle.centerYProperty().unbind();

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

        // Make the location draggable (if shift is not pressed, and there is no edge currently being drawn)
        DragHelper.makeDraggable(this, (event) -> !event.isShiftDown() && !ModelCanvas.edgeIsBeingDrawn() && !this.equals(ModelCanvas.getLocationOnMouse()));

        // Draw a new edge from the location
        localMouseTracker.registerOnMousePressedEventHandler(event -> {
            if (event.isShiftDown() && !ModelCanvas.edgeIsBeingDrawn()) {
                final Edge edge = new Edge(this, canvasMouseTracker);
                addChildToParent(edge);
            }
        });

        // Add keybind to discard the location if ESC is pressed
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_LOCATION, removeOnEscape);

        // Register toggle urgent and committed keybinds when the locations is hovered, and unregister them when we are not
        localMouseTracker.registerOnMouseEnteredEventHandler(event -> {
            KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_URGENT, makeLocationUrgent);
            KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED, makeLocationCommitted);
        });
        localMouseTracker.registerOnMouseExitedEventHandler(event -> {
            KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_URGENT);
            KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED);
        });
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        // Get the parent and detach this Location
        IParent parent = (IParent) this.getParent();
        if (parent == null) return;

        parent.removeChild(this);

        // Notify the canvas that we are no longer placing a location
        ModelCanvas.setLocationOnMouse(null);
    });

    private final Keybind makeLocationUrgent = new Keybind(new KeyCodeCombination(KeyCode.U), () -> {
        // The location cannot be committed
        isCommitted.set(false);

        // Toggle the urgent boolean
        isUrgent.set(!isUrgent.get());
    });

    private final Keybind makeLocationCommitted = new Keybind(new KeyCodeCombination(KeyCode.C), () -> {
        // The location cannot be committed
        isUrgent.set(false);

        // Toggle the urgent boolean
        isCommitted.set(!isCommitted.get());
    });

    private void addChildToParent(final Node child) {
        // Get the parent from the source location
        IParent parent = (IParent) this.getParent();

        if (parent == null) return;

        parent.addChild(child);
    }


    @Override
    public MouseTracker getMouseTracker() {
        return this.localMouseTracker;
    }

    @Override
    public DoubleProperty xProperty() {
        return circle.centerXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return circle.centerYProperty();
    }
}
