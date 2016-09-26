package SW9.model_canvas;

import SW9.MouseTracker;
import SW9.utility.DragHelper;
import javafx.beans.property.DoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class Nail extends Circle implements MouseTracker.hasMouseTracker {

    private final static double RADIUS = 5d;
    private final MouseTracker mouseTracker = new MouseTracker(this);

    public Nail(final double centerX, final double centerY) {
        super(centerX, centerY, RADIUS);
        this.setFill(Color.grayRgb(100, 0.5));

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
}