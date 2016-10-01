package SW9.model_canvas.locations;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.issues.Warning;
import SW9.model_canvas.IParent;
import SW9.model_canvas.ModelCanvas;
import SW9.model_canvas.ModelContainer;
import SW9.model_canvas.Parent;
import SW9.model_canvas.edges.Edge;
import SW9.utility.BindingHelper;
import SW9.utility.DragHelper;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import jiconfont.icons.GoogleMaterialDesignIcons;
import jiconfont.javafx.IconNode;

public class Location extends Parent implements DragHelper.Draggable {

    // Used to create the Location
    public final static double RADIUS = 25.0f;

    // Used to update the interaction with the mouse
    private BooleanProperty isOnMouse = new SimpleBooleanProperty(false);
    public final MouseTracker localMouseTracker;

    public Circle circle;
    public Label locationLabel;

    public BooleanProperty isUrgent = new SimpleBooleanProperty(false);
    public BooleanProperty isCommitted = new SimpleBooleanProperty(false);

    public enum Type {
        NORMAL, INITIAL, FINAL
    }

    public Type type;

    private void initializeWarnings() {
        // Warn the user when the location is initial
        final Warning isInitialLocation = new Warning<>(location -> location.type.equals(Type.INITIAL), this);
        final IconNode isInitialLocationIcon = isInitialLocation.generateIconNode();
        BindingHelper.bind(isInitialLocationIcon, this);

        addChildren(isInitialLocationIcon);
    }

    private void initializeErrors() {
        // Warn the user when the location is initial
        final SW9.issues.Error isExitLocation = new SW9.issues.Error<>(location -> location.type.equals(Type.FINAL), this);
        final IconNode isExitLocationIcon = isExitLocation.generateIconNode();
        BindingHelper.bind(isExitLocationIcon, this);

        addChildren(isExitLocationIcon);
    }


    public Location(MouseTracker canvasMouseTracker) {
        this(canvasMouseTracker.getXProperty(), canvasMouseTracker.getYProperty(), canvasMouseTracker, Type.NORMAL);

        // It is initialize with the mouse, hence it is on the mouse
        isOnMouse.set(true);

        // Only locations following on the mouse is discardable (until placed)
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_LOCATION, removeOnEscape);
    }

    public Location(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY, final MouseTracker canvasMouseTracker, Type type) {
        // Initialize the type property
        this.type = type;

        // Bind the mouse transparency to the boolean telling if the location is on the mouse
        this.mouseTransparentProperty().bind(isOnMouse);

        // Add the circle and add it at a child
        circle = new Circle(centerX.doubleValue(), centerY.doubleValue(), RADIUS);
        addChild(circle);

        // If the location is not a normal locations draw the visual cues
        if (type == Type.INITIAL) {
            addChild(new InitialLocationCircle(this));
        } else if (type == Type.FINAL) {
            addChild(new FinalLocationCross(this));
        }

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
        canvasMouseTracker.registerOnMousePressedEventHandler(mouseClickedEvent -> {
            if (isOnMouse.get() && ModelCanvas.mouseIsHoveringModelContainer()) {

                ModelContainer modelContainer = ModelCanvas.getHoveredModelContainer();

                circle.centerXProperty().bind(modelContainer.xProperty().add(mouseClickedEvent.getX() - (modelContainer.xProperty().get())));
                circle.centerYProperty().bind(modelContainer.yProperty().add(mouseClickedEvent.getY() - (modelContainer.yProperty().get())));

                modelContainer.addLocation(this);

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
                    isOnMouse.set(false);
                });

                locationPlaceAnimation.play();
            }
        });

        // Make the location draggable (if shift is not pressed, and there is no edge currently being drawn)
        DragHelper.makeDraggable(this, (event) -> {

            ModelContainer parent = (ModelContainer) getParent();
            boolean allowX = event.getX() - RADIUS >= parent.xProperty().get() && event.getX() < parent.xProperty().get() + parent.getXLimit().get() - RADIUS;
            boolean allowY = event.getY() - RADIUS >= parent.yProperty().get() && event.getY() < parent.yProperty().get() + parent.getYLimit().get() - RADIUS;


            return !event.isShiftDown() &&
                    !ModelCanvas.edgeIsBeingDrawn() &&
                    !this.equals(ModelCanvas.getLocationOnMouse()) &&
                    type.equals(Type.NORMAL) &&
                    allowX &&
                    allowY;
        });

        // Draw a new edge from the location
        localMouseTracker.registerOnMousePressedEventHandler(event -> {
            if (event.isShiftDown() && !ModelCanvas.edgeIsBeingDrawn()) {
                final Edge edge = new Edge(this, canvasMouseTracker);
                addChildToParent(edge);
            }
        });

        // Register toggle urgent and committed keybinds when the locations is hovered, and unregister them when we are not
        localMouseTracker.registerOnMouseEnteredEventHandler(event -> {
            KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_URGENT, makeLocationUrgent);
            KeyboardTracker.registerKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED, makeLocationCommitted);
        });
        localMouseTracker.registerOnMouseExitedEventHandler(event -> {
            KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_URGENT);
            KeyboardTracker.unregisterKeybind(KeyboardTracker.MAKE_LOCATION_COMMITTED);
        });

        // Initialize errors and warnings (must happen after the rest of the initializations)
        initializeErrors();
        initializeWarnings();
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
