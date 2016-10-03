package SW9.model_canvas.edges;

import SW9.MouseTracker;
import SW9.model_canvas.IChild;
import SW9.model_canvas.IParent;
import SW9.model_canvas.Parent;
import SW9.utility.DragHelper;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.Circle;

public class Nail extends Circle implements DragHelper.Draggable {

    private final static double HIDDEN_RADIUS = 0d;
    private final static double VISIBLE_RADIUS = 5d;
    private final MouseTracker mouseTracker = new MouseTracker(this);

    public boolean isBeingDragged = false;

    public Nail(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY) {
        super(centerX.get(), centerY.get(), HIDDEN_RADIUS);

        xProperty().bind(centerX);
        yProperty().bind(centerY);

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