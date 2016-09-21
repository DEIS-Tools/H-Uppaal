package SW9.model_canvas;

import SW9.Keybind;
import SW9.KeyboardTracker;
import SW9.Main;
import SW9.utility.DropShadowHelper;
import javafx.animation.Animation;
import javafx.animation.Transition;
import javafx.beans.property.BooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.layout.Pane;
import javafx.util.Duration;

public class ModelCanvas extends Pane {

    private static Location locationOnMouse = null;
    private static Edge edgeOnMouse = null;
    private static Location hoveredLocation = null;

    public static boolean mouseHasLocation() {
        return locationOnMouse != null;
    }

    public static boolean mouseHasEdge() {
        return edgeOnMouse != null;
    }

    public static boolean locationIsHovered() {
        return hoveredLocation != null;
    }

    public ModelCanvas() {
        initialize();
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

    public static Location getHoveredLocation() {
        return hoveredLocation;
    }

    public static void setHoveredLocation(final Location location) {
        final Location prevHovered = getHoveredLocation();

        if(prevHovered != location) {

            if(prevHovered != null) {
                new Transition() {
                    {
                        setCycleDuration(Duration.millis(100));
                    }

                    protected void interpolate(double frac) {
                        prevHovered.setEffect(DropShadowHelper.generateElevationShadow(6 - 6 * frac));
                    }
                }.play();
            }

            if(location != null) {
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

    public static Edge getEdgeOnMouse() {
        return edgeOnMouse;
    }

    public static void setEdgeOnMouse(final Edge edge) {
        edgeOnMouse = edge;
    }

    public static Location getLocationOnMouse() {
        return locationOnMouse;
    }

    public static void setLocationOnMouse(final Location location) {
        locationOnMouse = location;
    }

}
