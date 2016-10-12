package SW9.model_canvas.locations;

import SW9.issues.Warning;
import SW9.model_canvas.*;
import SW9.model_canvas.edges.Edge;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.*;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.*;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import jiconfont.javafx.IconNode;

import java.util.ArrayList;
import java.util.List;

public class Location extends Parent implements MouseTrackable, Removable, Colorable {

    // Modelling properties
    private final StringProperty nameProperty = new SimpleStringProperty();
    private final StringProperty invariantProperty = new SimpleStringProperty();
    private final BooleanProperty initialProperty = new SimpleBooleanProperty();
    private final BooleanProperty urgentProperty = new SimpleBooleanProperty();
    private final BooleanProperty committedProperty = new SimpleBooleanProperty();

    // UI properties
    private Color color = null;
    private Color.Intensity intensity = null;
    private boolean colorIsSet = false;

    // Used to create the Location
    public final static double RADIUS = 25.0f;

    // Used to update the interaction with the mouse
    private final BooleanProperty isOnMouse = new SimpleBooleanProperty(false);
    private final MouseTracker localMouseTracker;

    public final Circle circle;
    private final Label locationLabel;
    private InitialLocationCircle initialLocationCircle = null;
    private FinalLocationCross finalLocationCross = null;

    private ModelContainer modelContainer;
    private List<Edge> deletedEdges = new ArrayList<>();

    public enum Type {
        NORMAL, INITIAL, FINAL
    }

    public final Type type;

    private void initializeWarnings() {
        // Warn the user when the location is initial
        final Warning isInitialLocation = new Warning<>(location -> location.type.equals(Type.INITIAL), this);
        isInitialLocation.setMessage("Location is initial");
        final IconNode isInitialLocationIcon = isInitialLocation.generateIconNode();
        BindingHelper.bind(isInitialLocationIcon, this);

        addChildren(isInitialLocationIcon);
    }

    private void initializeErrors() {
        // Warn the user when the location is exit
        final SW9.issues.Error isExitLocation = new SW9.issues.Error<>(location -> location.type.equals(Type.FINAL), this);
        isExitLocation.setMessage("Location is exit");
        final IconNode isExitLocationIcon = isExitLocation.generateIconNode();
        BindingHelper.bind(isExitLocationIcon, this);

        addChildren(isExitLocationIcon);
    }

    public Location(final MouseTracker canvasMouseTracker) {
        this(canvasMouseTracker.xProperty(), canvasMouseTracker.yProperty(), canvasMouseTracker, Type.NORMAL, null);

        // It is initialize with the mouse, hence it is on the mouse
        isOnMouse.set(true);

        // Only locations following on the mouse is discardable (until placed)
        KeyboardTracker.registerKeybind(KeyboardTracker.DISCARD_NEW_LOCATION, removeOnEscape);
    }

    public Location(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY, final MouseTracker canvasMouseTracker, Type type, final ModelContainer modelContainer) {
        // Initialize the model container property
        this.modelContainer = modelContainer;

        // Initialize the type property
        this.type = type;

        // Bind the mouse transparency to the boolean telling if the location is on the mouse
        this.mouseTransparentProperty().bind(isOnMouse);

        // Add the circle and add it at a child
        circle = new Circle(centerX.doubleValue(), centerY.doubleValue(), RADIUS);
        addChild(circle);

        // If the location is not a normal locations draw the visual cues
        if (type == Type.INITIAL) {
            initialLocationCircle = new InitialLocationCircle(this);
            addChild(initialLocationCircle);
        } else if (type == Type.FINAL) {
            finalLocationCross = new FinalLocationCross(this);
            addChild(finalLocationCross);
        }

        // Add a text which we will use as a label for urgent and committed locations
        locationLabel = new Label();
        locationLabel.textProperty().bind(new StringBinding() {
            {
                super.bind(urgentProperty, committedProperty);
            }

            @Override
            protected String computeValue() {
                if (urgentProperty.get()) {
                    return "U";
                } else if (committedProperty.get()) {
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
        canvasMouseTracker.registerOnMousePressedEventHandler(event -> {
            if (isOnMouse.get() && ModelCanvas.mouseIsHoveringModelContainer()) {
                // Consume the event
                event.consume();

                // Find the component that the location should be created inside
                this.modelContainer = ModelCanvas.getHoveredModelContainer();

                // Bind the circle
                circle.centerXProperty().bind(this.modelContainer.xProperty().add(canvasMouseTracker.xProperty().get() - (this.modelContainer.xProperty().get())));
                circle.centerYProperty().bind(this.modelContainer.yProperty().add(canvasMouseTracker.yProperty().get() - (this.modelContainer.yProperty().get())));

                // Tell the canvas that the mouse is no longer occupied
                ModelCanvas.setLocationOnMouse(null);

                // Disable discarding functionality for a location when it is placed on the canvas
                KeyboardTracker.unregisterKeybind(KeyboardTracker.DISCARD_NEW_LOCATION);

                // Animate the way the location is placed on the canvas
                Animation locationPlaceAnimation = new Transition() {
                    {
                        setCycleDuration(Duration.millis(50));
                    }

                    protected void interpolate(double fraction) {
                        Location.this.setEffect(DropShadowHelper.generateElevationShadow(12 - 12 * fraction));
                    }
                };

                // When the animation is done ensure that we note that we are no longer on the mouse
                locationPlaceAnimation.setOnFinished(e -> isOnMouse.set(false));
                locationPlaceAnimation.play();

                UndoRedoStack.push(() -> { // Perform
                    resetColor(this.modelContainer.getColor(), this.modelContainer.getColorIntensity());
                    this.modelContainer.add(this);
                }, () -> { // Undo
                    resetColor();
                    this.modelContainer.remove(this);
                });
            }
        });

        makeDraggable();

        SelectHelper.makeSelectable(this);

        // Draw a new edge from the location
        localMouseTracker.registerOnMousePressedEventHandler(event -> {
            if (event.isShiftDown() && !ModelCanvas.edgeIsBeingDrawn()) {
                final Edge edge = new Edge(this, canvasMouseTracker);

                UndoRedoStack.push(() -> { // Perform
                    this.modelContainer.add(edge);
                }, () -> { // Undo
                    this.modelContainer.remove(edge);
                });
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

        // Will color the location
        resetColor();
    }

    private void makeDraggable() {
        ModelContainer parent = (ModelContainer) getParent();

        parentProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || !(newValue instanceof ModelComponent)) return;

            DragHelper.makeDraggable(this,
                    (event) -> !event.isShiftDown() &&
                            !ModelCanvas.edgeIsBeingDrawn() &&
                            !this.equals(ModelCanvas.getLocationOnMouse()) &&
                            type.equals(Type.NORMAL),
                    ((ModelComponent) newValue)::getInternalBounds
            );
        });
    }

    private final Keybind removeOnEscape = new Keybind(new KeyCodeCombination(KeyCode.ESCAPE), () -> {
        // Get the parent and detach this Location
        IParent parent = (IParent) this.getParent();
        if (parent == null) return;

        parent.removeChild(this);

        // Notify the canvas that we are no longer placing a location
        ModelCanvas.setLocationOnMouse(null);

        // Tell this instance that it is no longer on the mouse
        isOnMouse.set(false);
    });

    private final Keybind makeLocationUrgent = new Keybind(new KeyCodeCombination(KeyCode.U), () -> {
        final boolean wasUrgentBefore = urgentProperty.get();
        final boolean wasCommittedBefore = committedProperty.get();

        UndoRedoStack.push(() -> { // Perform
            // The location cannot be committed
            committedProperty.set(false);

            // Toggle the urgent boolean
            urgentProperty.set(!urgentProperty.get());
        }, () -> { // Undo
            urgentProperty.set(wasUrgentBefore);
            committedProperty.set(wasCommittedBefore);
        });

    });

    private final Keybind makeLocationCommitted = new Keybind(new KeyCodeCombination(KeyCode.C), () -> {
        final boolean wasUrgentBefore = urgentProperty.get();
        final boolean wasCommittedBefore = committedProperty.get();

        UndoRedoStack.push(() -> { // Perform
            // The location cannot be committed
            urgentProperty.set(false);

            // Toggle the urgent boolean
            committedProperty.set(!committedProperty.get());
        }, () -> { // Undo
            committedProperty.set(wasCommittedBefore);
            urgentProperty.set(wasUrgentBefore);
        });
    });

    private void addChildToParent(final Node child) {
        // Get the parent from the source location
        IParent parent = (IParent) this.getParent();

        if (parent == null) return;

        parent.addChild(child);
    }

    private void removeChildFromParent(final Node child) {
        // Get the parent from the source location
        IParent parent = (IParent) this.getParent();

        if (parent == null) return;

        parent.removeChild(child);
    }

    public ModelContainer getModelContainer() {
        return modelContainer;
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

    @Override
    public boolean select() {
        if (this.type.equals(Type.NORMAL)) {
            circle.getStyleClass().add("selected");
            locationLabel.getStyleClass().add("selected");
            return true;
        }

        return false;
    }

    @Override
    public void deselect() {
        circle.getStyleClass().remove("selected");
        locationLabel.getStyleClass().remove("selected");
    }

    @Override
    public boolean remove() {
        deletedEdges = new ArrayList<>(modelContainer.getEdges(this));
        modelContainer.remove(this);

        return true;
    }

    @Override
    public void reAdd() {
        modelContainer.add(this);
        deletedEdges.forEach(edge -> modelContainer.add(edge));
    }

    @Override
    public boolean isColored() {
        return colorIsSet;
    }

    @Override
    public Color getColor() {
        return color;
    }

    @Override
    public Color.Intensity getColorIntensity() {
        return intensity;
    }

    @Override
    public boolean color(final Color color, final Color.Intensity intensity) {
        // If the color should not be changed, do nothing
        if (color.equals(getColor()) && intensity.equals(getColorIntensity())) {
            return false;
        }

        colorIsSet = true;

        this.color = color;
        this.intensity = intensity;

        circle.setFill(color.getColor(intensity));
        circle.setStroke(color.getColor(intensity.next(2)));
        locationLabel.setTextFill(color.getTextColor(intensity));

        if (initialLocationCircle != null) {
            initialLocationCircle.getInnerCircle().setStroke(color.getTextColor(intensity));
        }

        if (finalLocationCross != null) {
            finalLocationCross.getFirstLine().setStroke(color.getTextColor(intensity));
            finalLocationCross.getSecondLine().setStroke(color.getTextColor(intensity));
        }

        return true;
    }

    @Override
    public void resetColor() {
        resetColor(Color.GREY_BLUE, Color.Intensity.I700); // default color
    }

    @Override
    public void resetColor(final Color color, final Color.Intensity intensity) {
        color(color, intensity);
        colorIsSet = false;
    }

    // Modelling accessors
    public StringProperty nameProperty() {
        return nameProperty;
    }

    public String getName() {
        return nameProperty().get();
    }

    public StringProperty invariantProperty() {
        return invariantProperty;
    }

    public String getInvariant() {
        return invariantProperty().get();
    }

    public BooleanProperty initialProperty() {
        return initialProperty;
    }

    public Boolean isInitial() {
        return initialProperty().get();
    }

    public BooleanProperty urgentProperty() {
        return urgentProperty;
    }

    public Boolean isUrgent() {
        return urgentProperty().get();
    }

    public BooleanProperty committedProperty() {
        return committedProperty;
    }

    public Boolean isCommitted() {
        return committedProperty().get();
    }
}
