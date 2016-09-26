package SW9.model_canvas;

import SW9.MouseTracker;
import SW9.utility.DragHelper;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Nail extends Circle implements MouseTracker.hasMouseTracker {

    private final static double HIDDEN_RADIUS = 0d;
    private final static double VISIBLE_RADIUS = 5d;
    private final MouseTracker mouseTracker = new MouseTracker(this);

    public boolean isBeingDragged = false;

    public Nail(final double centerX, final double centerY) {
        super(centerX, centerY, HIDDEN_RADIUS);

        // Style the nail
        getStyleClass().add("nail");

        // Hide the nails so that they do not become rendered right away
        visibleProperty().setValue(false);

        // Bind the radius to the visibility property (so that we do not get space between links)
        radiusProperty().bind(new When(visibleProperty()).then(VISIBLE_RADIUS).otherwise(HIDDEN_RADIUS));

        DragHelper.makeDraggable(this);

        mouseTracker.registerOnMousePressedEventHandler(event -> isBeingDragged = true);
        mouseTracker.registerOnMouseReleasedEventHandler(event -> isBeingDragged = false);
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
    }

    @Override
    public DoubleProperty xProperty() {
        return centerXProperty();
    }

    @Override
    public DoubleProperty yProperty() {
        return centerYProperty();
    }
}