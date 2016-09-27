package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.Main;
import SW9.utility.DropShadowHelper;
import javafx.animation.Transition;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ModelCanvas extends Pane {


    // Variables describing the state of the canvas
    private static Location locationOnMouse = null;
    private static Location hoveredLocation = null;
    private static Edge edgeBeingDrawn = null;

    public ModelCanvas() {
        initialize();
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

    /**
     * Checks if an Edge is currently being drawn on the canvas
     * This functionality is maintained by the Edge itself.
     *
     * @return true if an Edge is being drawn, otherwise false
     */
    public static boolean edgeIsBeingDrawn() {
        return edgeBeingDrawn != null;
    }

    @FXML
    public void initialize() {
        KeyboardTracker.registerKeybind(KeyboardTracker.ADD_NEW_LOCATION, new Keybind(new KeyCodeCombination(KeyCode.L), () -> {
            if (!mouseHasLocation()) {
                final Location newLocation = new Location(Main.mouseTracker);
                locationOnMouse = newLocation;

                newLocation.setEffect(DropShadowHelper.generateElevationShadow(22));
                ModelCanvas.this.getChildren().add(newLocation);
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

}
