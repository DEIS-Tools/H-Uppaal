package SW9.model_canvas.arrow_heads;

import SW9.utility.helpers.LocationAware;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class ArrowHead extends Group implements LocationAware {

    private final DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    private final DoubleProperty yProperty = new SimpleDoubleProperty(0d);

    public ArrowHead() {
        // Calculate the rotation body (a fix to move the point of rotation)
        Rectangle rotationBody = new Rectangle();
        rotationBody.xProperty().bind(xProperty().subtract(getHeadWidth() / 2));
        rotationBody.yProperty().bind(yProperty().subtract(getHeadHeight()));
        rotationBody.widthProperty().set(getHeadWidth());
        rotationBody.heightProperty().set(getHeadHeight() * 2);
        rotationBody.setMouseTransparent(true);
        rotationBody.setFill(Color.TRANSPARENT);

        // Add the body to the head
        getChildren().add(rotationBody);
    }

    public abstract double getHeadHeight();

    public abstract double getHeadWidth();

    public boolean shouldBindToTip() {
        return false;
    }

    public abstract void mark();

    public abstract void unmark();

    @Override
    public DoubleProperty xProperty() {
        return xProperty;
    }

    @Override
    public DoubleProperty yProperty() {
        return yProperty;
    }
}
