package SW9.model_canvas.edges;

import SW9.MouseTracker;
import SW9.model_canvas.Removable;
import SW9.utility.DragHelper;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.Parent;
import javafx.scene.shape.Circle;

public class Nail extends Circle implements Removable {

    private final static double HIDDEN_RADIUS = 0d;
    private final static double VISIBLE_RADIUS = 5d;
    private final MouseTracker mouseTracker = new MouseTracker(this);

    private Edge detachedParent;
    int restoreIndex;

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

        mouseTracker.registerOnMousePressedEventHandler(event -> isBeingDragged = true);
        mouseTracker.registerOnMouseReleasedEventHandler(event -> isBeingDragged = false);

        DragHelper.makeDraggable(this);
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

    @Override
    public boolean select() {
        detachedParent = getEdgeParent();
        detachedParent.nailWasSelected = true;
        System.out.println("nail select");
        getStyleClass().add("selected");
        return true;
    }

    @Override
    public void deselect() {
        detachedParent.nailWasSelected = false;
        System.out.println("nail deselect");
        getStyleClass().remove("selected");
    }

    @Override
    public void remove() {
        getEdgeParent().remove(this);
    }

    @Override
    public void reAdd() {
        detachedParent.add(this, restoreIndex);
    }

    private Edge getEdgeParent() {
        Parent parent = getParent();
        while (parent != null) {
            if (parent instanceof Edge) {
                return ((Edge) parent);

            }
            parent = parent.getParent();
        }
        return null;
    }
}