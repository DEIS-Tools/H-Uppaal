package SW9.model_canvas;

import SW9.MouseTracker;
import javafx.scene.shape.Circle;

public class Location extends Circle {

    private final static double RADIUS = 25.0f;

    public final MouseTracker mouseTracker = new MouseTracker();

    private void initializeMouseTracker() {
        this.setOnMouseMoved(mouseTracker.onMouseMovedEventHandler);
        this.setOnMouseClicked(mouseTracker.onMouseClickedEventHandler);
    }

    public Location(final double centerX, final double centerY) {
        super(centerX, centerY, RADIUS);

        initializeMouseTracker();

        this.getStyleClass().add("location");
    }

}
