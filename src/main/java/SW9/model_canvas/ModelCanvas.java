package SW9.model_canvas;

import SW9.model_canvas.arrow_heads.ArrowHead;
import SW9.model_canvas.arrow_heads.BroadcastChannelSenderArrowHead;
import SW9.model_canvas.arrow_heads.ChannelReceiverArrowHead;
import SW9.model_canvas.arrow_heads.HandshakeChannelSenderArrowHead;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.edges.Properties;
import SW9.model_canvas.locations.Location;
import SW9.model_canvas.synchronization.ChannelBox;
import SW9.utility.UndoRedoStack;
import SW9.utility.helpers.*;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Duration;

import java.util.ArrayList;

public class ModelCanvas extends Pane implements MouseTrackable, IParent {

    public static final int GRID_SIZE = 25;

    // Variables describing the state of the canvas
    private static Location locationOnMouse = null;
    private static Location hoveredLocation = null;
    private static Edge edgeBeingDrawn = null;
    private static ModelContainer hoveredModelContainer = null;

    private final MouseTracker mouseTracker = new MouseTracker(this);

    public ModelCanvas() {
        initialize();

        // This is a fix to make the canvas larger when we translate it away from 0,0
        final Circle canvasExpanderNode = new Circle();
        canvasExpanderNode.radiusProperty().set(0);
        addChild(canvasExpanderNode);
        canvasExpanderNode.translateXProperty().bind(this.translateXProperty().multiply(-1));
        canvasExpanderNode.translateYProperty().bind(this.translateYProperty().multiply(-1));

        // Add a grid to the canvas
        final Grid grid = new Grid(GRID_SIZE);
        getChildren().add(grid);

        DragHelper.makeDraggable(this, mouseEvent -> mouseEvent.getButton().equals(MouseButton.SECONDARY));
    }

    @FXML
    public void initialize() {
        KeyboardTracker.registerKeybind(KeyboardTracker.ADD_CHANNEL_BOX, new Keybind(new KeyCodeCombination(KeyCode.B), () -> {
            final ChannelBox channelBox = new ChannelBox();

            UndoRedoStack.push(() -> { // Perform
                addChild(channelBox);
            }, () -> { // Undo
                removeChild(channelBox);
            });
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.DELETE_SELECTED, new Keybind(new KeyCodeCombination(KeyCode.DELETE), () -> {
            ArrayList<Removable> copy = new ArrayList<>();

            SelectHelper.getSelectedElements().forEach(copy::add);

            SelectHelper.clearSelectedElements();

            UndoRedoStack.push(() -> { // Perform
                copy.forEach(Removable::remove);
            }, () -> { // Undo
                copy.forEach(Removable::deselect);
                copy.forEach(Removable::reAdd);
            });
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.UNDO, new Keybind(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN), UndoRedoStack::undo));
        KeyboardTracker.registerKeybind(KeyboardTracker.REDO, new Keybind(new KeyCodeCombination(KeyCode.Z, KeyCombination.SHORTCUT_DOWN, KeyCombination.SHIFT_DOWN), UndoRedoStack::redo));

        KeyboardTracker.registerKeybind(KeyboardTracker.ADD_NEW_LOCATION, new Keybind(new KeyCodeCombination(KeyCode.L), () -> {
            if (!mouseHasLocation()) {
                final Location newLocation = new Location(mouseTracker);
                locationOnMouse = newLocation;
                newLocation.setEffect(DropShadowHelper.generateElevationShadow(22));
                addChild(newLocation);
            }
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.CREATE_COMPONENT, new Keybind(new KeyCodeCombination(KeyCode.K), () -> {
            final ModelComponent mc = new ModelComponent(mouseTracker.xProperty().get(), mouseTracker.yProperty().get(), 400, 600, "Component", mouseTracker);

            UndoRedoStack.push(() -> addChild(mc), () -> removeChild(mc));
        }));

        // TODO remove me when testing of heads is done
        KeyboardTracker.registerKeybind(KeyboardTracker.TESTING_BIND, new Keybind(new KeyCodeCombination(KeyCode.T), () -> {
            // Outgoing arrows
            final Circle outgoingStart1 = new Circle(100, 100, 0);
            final Circle outgoingEnd1 = new Circle(200, 100, 0);

            final Circle outgoingStart2 = new Circle(100, 200, 0);
            final Circle outgoingEnd2 = new Circle(200, 200, 0);

            final ArrowHead handshakeArrowHead = new HandshakeChannelSenderArrowHead();
            final ArrowHead broadCastArrowHead = new BroadcastChannelSenderArrowHead();
            handshakeArrowHead.isUrgentProperty().setValue(true);


            final Line handshakeArrowLine = new Line();
            final Line broadCastArrowLine = new Line();

            BindingHelper.bind(handshakeArrowLine, outgoingStart1, outgoingEnd1);
            BindingHelper.bind(broadCastArrowLine, outgoingStart2, outgoingEnd2);

            BindingHelper.bind(handshakeArrowHead, outgoingStart1, outgoingEnd1);
            BindingHelper.bind(broadCastArrowHead, outgoingStart2, outgoingEnd2);

            BindingHelper.bind(handshakeArrowLine, handshakeArrowHead);
            BindingHelper.bind(broadCastArrowLine, broadCastArrowHead);

            // Incoming arrows
            final Circle incomingEnd1 = new Circle(300, 100, 0);
            final Circle incomingStart1 = new Circle(400, 100, 0);

            final Line channelReceiverLine = new Line();
            final ArrowHead channelReceiverArrowHead = new ChannelReceiverArrowHead();

            BindingHelper.bind(channelReceiverLine, incomingStart1, incomingEnd1);
            BindingHelper.bind(channelReceiverArrowHead, incomingStart1, incomingEnd1);
            BindingHelper.bind(channelReceiverLine, channelReceiverArrowHead);

            // Properties
            Properties properties = new Properties(new SimpleDoubleProperty(100), new SimpleDoubleProperty(300));

            UndoRedoStack.push(
                    () -> addChildren(handshakeArrowLine, broadCastArrowLine, handshakeArrowHead, broadCastArrowHead, properties, channelReceiverArrowHead, channelReceiverLine),
                    () -> removeChildren(handshakeArrowLine, broadCastArrowLine, handshakeArrowHead, broadCastArrowHead, properties, channelReceiverArrowHead, channelReceiverLine)
            );
        }));

    }

    /**
     * Gets the Location that currently follows the mouse on the canvas
     * This functionality is maintained by the Location themselves.
     *
     * @return the Locations following the mouse on the canvas
     */
    public static Location getLocationOnMouse() {
        return locationOnMouse;
    }

    /**
     * Sets a Location to follow the mouse on the canvas
     * This functionality is maintained by the Location themselves.
     *
     * @param location - a Location to follow the mouse
     */
    public static void setLocationOnMouse(final Location location) {
        locationOnMouse = location;
    }

    /**
     * Checks if a Location is currently following the mouse on the canvas
     * This functionality is maintained by the Location themselves.
     *
     * @return true if a Location is following the mouse, otherwise false
     */
    public static boolean mouseHasLocation() {
        return locationOnMouse != null;
    }

    /**
     * Gets the Location currently being hovered by the mouse on the canvas
     * This functionality is maintained by the Location themselves.
     *
     * @return the Location be hovered on the canvas
     */
    public static Location getHoveredLocation() {
        return hoveredLocation;
    }

    /**
     * Sets a Location to currently being hovered by the mouse on the canvas
     * This functionality is maintained by the Location themselves.
     *
     * @param location - a Location that is being hovered
     */
    public static void setHoveredLocation(final Location location) {
        final Location prevHovered = getHoveredLocation();

        if (prevHovered != location) {

            if (prevHovered != null) {
                new Transition() {
                    {
                        setCycleDuration(Duration.millis(100));
                    }

                    protected void interpolate(double fraction) {
                        prevHovered.setEffect(DropShadowHelper.generateElevationShadow(6 - 6 * fraction));
                    }
                }.play();
            }

            if (location != null) {
                new Transition() {
                    {
                        setCycleDuration(Duration.millis(100));
                    }

                    protected void interpolate(double frac) {
                        location.setEffect(DropShadowHelper.generateElevationShadow(6 * frac));
                    }
                }.play();
            }

            hoveredLocation = location;
        }
    }

    /**
     * Checks if a Location is currently being hovered by the mouse on the canvas
     * This functionality is maintained by the Location themselves.
     *
     * @return true if a Location is being hovered bt the mouse, otherwise false
     */
    public static boolean mouseIsHoveringLocation() {
        return hoveredLocation != null;
    }

    /**
     * Checks if an Edge is currently being drawn on the canvas
     * This functionality is maintained by the Edge itself.
     *
     * @return true if an Edge is being drawn, otherwise false
     */
    public static boolean edgeIsBeingDrawn() {
        return edgeBeingDrawn != null;
    }

    /**
     * Gets the Edge that is currently being drawn on the canvas
     * This functionality is maintained by the Edge itself.
     *
     * @return the Edge being drawn on the canvas
     */
    public static Edge getEdgeBeingDrawn() {
        return edgeBeingDrawn;
    }

    /**
     * Sets the Edge that is currently being drawn on the canvas
     * This functionality is maintained by the Edge itself.
     *
     * @param edgeBeingDrawn - the Edge being drawn
     */
    public static void setEdgeBeingDrawn(Edge edgeBeingDrawn) {
        ModelCanvas.edgeBeingDrawn = edgeBeingDrawn;
    }

    public static ModelContainer getHoveredModelContainer() {
        return hoveredModelContainer;
    }

    public static void setHoveredModelContainer(final ModelContainer modelContainer) {
        hoveredModelContainer = modelContainer;
    }

    public static boolean mouseIsHoveringModelContainer() {
        return hoveredModelContainer != null;
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    @Override
    public DoubleProperty xProperty() {
        return layoutXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return layoutYProperty();
    }

    @Override
    public void addChild(Node child) {
        getChildren().add(child);
    }

    @Override
    public void addChildren(Node... children) {
        getChildren().addAll(children);
    }

    @Override
    public void removeChild(Node child) {
        getChildren().remove(child);
    }

    @Override
    public void removeChildren(Node... children) {
        getChildren().removeAll(children);
    }
}
