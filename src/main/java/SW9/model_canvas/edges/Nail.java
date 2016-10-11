package SW9.model_canvas.edges;

import SW9.model_canvas.Parent;
import SW9.model_canvas.Removable;
import SW9.utility.colors.Color;
import SW9.utility.colors.Colorable;
import SW9.utility.helpers.DragHelper;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.binding.When;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.Circle;

public class Nail extends Parent implements Removable, Colorable {

    private Color color = null;
    private Color.Intensity intensity = null;
    private boolean colorIsSet = false;

    private final static double HIDDEN_RADIUS = 0d;
    protected final static double VISIBLE_RADIUS = 7d;
    private final MouseTracker mouseTracker = new MouseTracker(this);

    public final Circle circle = new Circle();

    private Edge detachedParent;
    int restoreIndex;

    public boolean isBeingDragged = false;

    public Nail(final ObservableDoubleValue centerX, final ObservableDoubleValue centerY) {
        xProperty().bind(centerX);
        yProperty().bind(centerY);

        // Style the nail
        circle.getStyleClass().add("nail");

        // Hide the nails so that they do not become rendered right away
        circle.visibleProperty().setValue(false);

        // Bind the radius to the visibility property (so that we do not get space between links)
        circle.radiusProperty().bind(new When(circle.visibleProperty()).then(VISIBLE_RADIUS).otherwise(HIDDEN_RADIUS));

        mouseTracker.registerOnMousePressedEventHandler(event -> isBeingDragged = true);
        mouseTracker.registerOnMouseReleasedEventHandler(event -> isBeingDragged = false);

        // Update the hovered nail of the edge that this nail belong to
        mouseTracker.registerOnMouseEnteredEventHandler(e -> getEdgeParent().setHoveredNail(this));
        mouseTracker.registerOnMouseExitedEventHandler(e -> {
            if (this.equals(getEdgeParent().getHoveredNail())) {
                getEdgeParent().setHoveredNail(null);
            }
        });

        DragHelper.makeDraggable(this);

        addChild(circle);
    }

    @Override
    public MouseTracker getMouseTracker() {
        return mouseTracker;
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
        detachedParent = getEdgeParent();
        circle.getStyleClass().add("selected");
        return true;
    }

    @Override
    public void deselect() {
        circle.getStyleClass().remove("selected");
    }

    @Override
    public boolean remove() {
        return getEdgeParent().remove(this);
    }

    @Override
    public void reAdd() {
        detachedParent.add(this, restoreIndex);
    }

    private Edge getEdgeParent() {
        javafx.scene.Parent parent = getParent();
        while (parent != null) {
            if (parent instanceof Edge) {
                return ((Edge) parent);

            }
            parent = parent.getParent();
        }
        return null;
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
        circle.setFill(color.getColor(intensity));
        circle.setStroke(color.getColor(intensity.next(2)));

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
}