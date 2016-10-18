package SW9.model_canvas;

import SW9.backend.HUPPAALDocument;
import SW9.model_canvas.arrow_heads.BroadcastChannelSenderArrowHead;
import SW9.model_canvas.arrow_heads.ChannelReceiverArrowHead;
import SW9.model_canvas.arrow_heads.HandshakeChannelSenderArrowHead;
import SW9.model_canvas.arrow_heads.SimpleArrowHead;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.edges.Properties;
import SW9.model_canvas.lines.DashedLine;
import SW9.model_canvas.locations.Location;
import SW9.model_canvas.querying.QueryField;
import SW9.model_canvas.synchronization.ChannelBox;
import SW9.utility.UndoRedoStack;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.*;
import SW9.utility.keyboard.Keybind;
import SW9.utility.keyboard.KeyboardTracker;
import SW9.utility.mouse.MouseTracker;
import javafx.animation.Transition;
import javafx.beans.property.DoubleProperty;
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ModelCanvas extends Pane implements MouseTrackable, IParent {

    public static final int GRID_SIZE = 10;

    // Variables describing the state of the canvas
    private static Location locationOnMouse = null;
    private static Location hoveredLocation = null;
    private static Edge edgeBeingDrawn = null;
    private static Component hoveredComponent = null;

    private final MouseTracker mouseTracker = new MouseTracker(this);
    private int componentCount = 0;

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
            final ArrayList<Removable> copy = new ArrayList<>();

            SelectHelper.getSelectedElements().forEach(copy::add);

            SelectHelper.clearSelectedElements();

            UndoRedoStack.push(() -> { // Perform
                for (int i = 0; i < copy.size(); i++) {
                    Removable removable = copy.get(i);

                    // Check if we successfully removed the element
                    final boolean result = removable.remove();

                    // If we did not successfully removed the element, re-add the already deleted ones
                    if (!result) {
                        // Re-add elements
                        for(int j = i - 1; j >= 0; j--) {
                            removable = copy.get(j);
                            removable.reAdd();
                        }

                        // Rollback this perform (clear the history of this perform action)
                        UndoRedoStack.forget();
                    }
                }

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

                if (mouseIsHoveringModelContainer()) {
                    newLocation.resetColor(getHoveredComponent().getColor(), getHoveredComponent().getColorIntensity());
                }
            }
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.CREATE_COMPONENT, new Keybind(new KeyCodeCombination(KeyCode.K), () -> {
            final Component mc = new Component(mouseTracker.xProperty().get(), mouseTracker.yProperty().get(), 400, 600, "Component" + componentCount, mouseTracker);
            componentCount++;

            UndoRedoStack.push(() -> addChild(mc), () -> removeChild(mc));
        }));

        // TODO remove me when testing of heads is done
        KeyboardTracker.registerKeybind(KeyboardTracker.TESTING_BIND, new Keybind(new KeyCodeCombination(KeyCode.T), () -> {
            getChildren().stream().filter(child -> child instanceof Component).forEach(child -> {
                final Component container = ((Component) child);

                container.hasDeadlockProperty().set(!container.hasDeadlockProperty().get());

                container.getLocations().forEach(location -> {
                    location.reachabilityCertaintyProperty().set(100);
                    location.isReachableProperty().set(false);
                });

                HUPPAALDocument huppaalDocument = new HUPPAALDocument(container);
                try {
                    huppaalDocument.toHuuppaalFile();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

            // Outgoing arrows
            final Circle outgoingStart1 = new Circle(100, 100, 0);
            final Circle outgoingEnd1 = new Circle(200, 100, 0);

            final Circle outgoingStart2 = new Circle(100, 200, 0);
            final Circle outgoingEnd2 = new Circle(200, 200, 0);

            final HandshakeChannelSenderArrowHead handshakeArrowHead = new HandshakeChannelSenderArrowHead();
            final BroadcastChannelSenderArrowHead broadCastArrowHead = new BroadcastChannelSenderArrowHead();
            handshakeArrowHead.isUrgentProperty().setValue(true);

            final Line handshakeArrowLine = new Line();
            final Line broadCastArrowLine = new Line();

            BindingHelper.bind(handshakeArrowLine, handshakeArrowHead, outgoingStart1, outgoingEnd1);
            BindingHelper.bind(broadCastArrowLine, broadCastArrowHead, outgoingStart2, outgoingEnd2);


            // Incoming arrows
            final Circle incomingEnd1 = new Circle(300, 100, 0);
            final Circle incomingStart1 = new Circle(400, 100, 0);

            final Line channelReceiverLine = new Line();
            final ChannelReceiverArrowHead channelReceiverArrowHead = new ChannelReceiverArrowHead();

            BindingHelper.bind(channelReceiverLine, channelReceiverArrowHead, incomingStart1, incomingEnd1);

            // Lines between outgoing and incoming arrows
            final DashedLine handshakeSyncLine = new DashedLine();
            final SimpleArrowHead handshakeSyncArrowHead = new SimpleArrowHead();

            BindingHelper.bind(handshakeSyncLine, handshakeArrowHead, channelReceiverArrowHead);
            BindingHelper.bind(handshakeSyncArrowHead, outgoingEnd1, channelReceiverArrowHead.getCircle());

            // Properties
            final Properties properties = new Properties();
            properties.xProperty().set(100);
            properties.yProperty().set(300);

            UndoRedoStack.push(
                    () -> addChildren(handshakeArrowLine, broadCastArrowLine, handshakeArrowHead, broadCastArrowHead, properties, channelReceiverArrowHead, channelReceiverLine, handshakeSyncLine, handshakeSyncArrowHead),
                    () -> removeChildren(handshakeArrowLine, broadCastArrowLine, handshakeArrowHead, broadCastArrowHead, properties, channelReceiverArrowHead, channelReceiverLine, handshakeSyncLine, handshakeSyncArrowHead)
            );



        }));

        // Gets the first model container and checks for deadlock
        KeyboardTracker.registerKeybind(KeyboardTracker.COMPONENT_HAS_DEADLOCK, new Keybind(new KeyCodeCombination(KeyCode.D), () -> {


            List<Component> componentList = new ArrayList<>();
            for (Node child : getChildren()) {
                if (child instanceof Component) {
                    componentList.add((Component) child);
                }
            }

            if(!componentList.isEmpty()) {
                addChild(new QueryField(200, 200, componentList));
            }

        }));

        // Color keybinds below
        KeyboardTracker.registerKeybind(KeyboardTracker.COLOR_0, new Keybind(new KeyCodeCombination(KeyCode.DIGIT0), () -> {
            final Component hoveredComponent = getHoveredComponent();
            final Location hoveredLocation = getHoveredLocation();

            // Not hovering anything interesting
            if (hoveredComponent == null) return;

            final Colorable[] hoveredElement = {null};
            if (hoveredComponent != null) hoveredElement[0] = (Colorable) hoveredComponent;
            if (hoveredLocation != null) hoveredElement[0] = hoveredLocation;

            // Only reset the color, if the element is actually colored (do avoid redundant undo-elements on the stack
            if (hoveredElement[0].isColored()) {
                final Color previousColor = hoveredElement[0].getColor();
                final Color.Intensity previousIntensity = hoveredElement[0].getColorIntensity();

                UndoRedoStack.push(() -> { // Perform
                    hoveredElement[0].resetColor();
                }, () -> { // Undo
                    hoveredElement[0].color(previousColor, previousIntensity);
                });
            }
        }));

        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_1, KeyCode.DIGIT1, Color.RED, Color.Intensity.I700);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_2, KeyCode.DIGIT2, Color.PINK, Color.Intensity.I500);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_3, KeyCode.DIGIT3, Color.PURPLE, Color.Intensity.I500);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_4, KeyCode.DIGIT4, Color.INDIGO, Color.Intensity.I500);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_5, KeyCode.DIGIT5, Color.BLUE, Color.Intensity.I500);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_6, KeyCode.DIGIT6, Color.TEAL, Color.Intensity.I500);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_7, KeyCode.DIGIT7, Color.GREY, Color.Intensity.I600);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_8, KeyCode.DIGIT8, Color.GREEN, Color.Intensity.I700);
        registerKeyBoardColorKeyBind(KeyboardTracker.COLOR_9, KeyCode.DIGIT9, Color.BROWN, Color.Intensity.I500);
    }

    private void registerKeyBoardColorKeyBind(final String id, final KeyCode keyCode, final Color color, final Color.Intensity intensity) {
        KeyboardTracker.registerKeybind(id, new Keybind(new KeyCodeCombination(keyCode), () -> {
            final Component hoveredComponent = getHoveredComponent();
            final Location hoveredLocation = getHoveredLocation();

            // Not hovering anything interesting
            if (hoveredComponent == null && hoveredLocation == null) return;

            final Colorable[] hoveredElement = {null};
            if (hoveredComponent != null) hoveredElement[0] = hoveredComponent;
            if (hoveredLocation != null) hoveredElement[0] = hoveredLocation;

            final Color previousColor = hoveredElement[0].getColor();
            final Color.Intensity previousIntensity = hoveredElement[0].getColorIntensity();
            final boolean wasPreviouslyColors = hoveredElement[0].isColored();

            UndoRedoStack.push(() -> { // Perform
                final boolean result = hoveredElement[0].color(color, intensity);
                if (!result) {
                    UndoRedoStack.undo(); // We did not color the element, undo the action immediately
                }
            }, () -> { // Undo
                if (wasPreviouslyColors) {
                    hoveredElement[0].color(previousColor, previousIntensity);
                } else {
                    hoveredElement[0].resetColor(previousColor, previousIntensity);
                }
            });
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

    public static Component getHoveredComponent() {
        return hoveredComponent;
    }

    public static void setHoveredComponent(final Component component) {
        hoveredComponent = component;
    }

    public static boolean mouseIsHoveringModelContainer() {
        return hoveredComponent != null;
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
