package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.MouseTracker;
import SW9.model_canvas.edges.ChannelBroadcastHead;
import SW9.model_canvas.edges.ChannelHandshakeHead;
import SW9.model_canvas.edges.Edge;
import SW9.model_canvas.locations.Location;
import SW9.utility.BindingHelper;
import SW9.utility.DragHelper;
import SW9.utility.DropShadowHelper;
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

public class ModelCanvas extends Pane implements DragHelper.Draggable, IParent {

    public static int GRID_SIZE = 25;

    // Variables describing the state of the canvas
    private static Location locationOnMouse = null;
    private static Location hoveredLocation = null;
    private static Edge edgeBeingDrawn = null;
    private static ModelContainer hoveredModelContainer = null;

    // TODO - remove me eventually
    private static Line testLine = new Line();
    private Node testHead = null;
    private int testCounter = 0;

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
        // TODO Remove this binding eventually
        // Bind the test line
        testLine.setStartX(200);
        testLine.setStartY(200);
        testLine.endXProperty().bind(mouseTracker.getXProperty());
        testLine.endYProperty().bind(mouseTracker.getYProperty());

        KeyboardTracker.registerKeybind(KeyboardTracker.ADD_NEW_LOCATION, new Keybind(new KeyCodeCombination(KeyCode.L), () -> {
            if (!mouseHasLocation()) {
                final Location newLocation = new Location(mouseTracker);
                locationOnMouse = newLocation;

                newLocation.setEffect(DropShadowHelper.generateElevationShadow(22));
                ModelCanvas.this.addChild(newLocation);
            }
        }));

        KeyboardTracker.registerKeybind(KeyboardTracker.CREATE_COMPONENT, new Keybind(new KeyCodeCombination(KeyCode.K), () -> {
            ModelComponent mc = new ModelComponent(mouseTracker.getXProperty().get(), mouseTracker.getYProperty().get(), 400, 600, "Component", mouseTracker);
            addChild(mc);
        }));

        // TODO remove me when testing of heads is done
        KeyboardTracker.registerKeybind(KeyboardTracker.TEST_ARROW_ONE, new Keybind(new KeyCodeCombination(KeyCode.T, KeyCombination.SHIFT_ANY), () -> {

            if(!getChildren().contains(testLine)) {
                addChild(testLine);
            }

            if(testHead != null) {
                removeChild(testHead);
            }

            testCounter++;

            if(testCounter == 1) {
                ChannelHandshakeHead head = new ChannelHandshakeHead();
                testHead = head;
                addChild(head);
                BindingHelper.bind(head, testLine);
            } else if(testCounter == 2) {
                ChannelBroadcastHead head = new ChannelBroadcastHead();
                testHead = head;
                addChild(head);
                BindingHelper.bind(head, testLine);
            }else {
                removeChild(testLine);
                testCounter = 0;
            }
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

                    protected void interpolate(double frac) {
                        prevHovered.setEffect(DropShadowHelper.generateElevationShadow(6 - 6 * frac));
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
     * @return true if a Location is beging hovered bt the mouse, otherwise false
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
     * @return the Edge bewing drawn on the canvas
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
    public void removeChild(Node child) {
        getChildren().remove(child);
    }

    @Override
    public void addChildren(Node... children) {
        getChildren().addAll(children);
    }
}
