package SW9.model_canvas.arrow_heads;

import SW9.model_canvas.Parent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;

public abstract class ArrowHead extends Parent {

    public final DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    public final DoubleProperty yProperty = new SimpleDoubleProperty(0d);

    public ArrowHead() {
        // Calculate the rotation body (a fix to move the point of rotation)
        Rectangle rotationBody = new Rectangle();
        rotationBody.xProperty().bind(xProperty.subtract(getHeadWidth() / 2));
        rotationBody.yProperty().bind(yProperty.subtract(getHeadHeight()));
        rotationBody.widthProperty().set(getHeadWidth());
        rotationBody.heightProperty().set(getHeadHeight() * 2);
        rotationBody.setMouseTransparent(true);
        rotationBody.setFill(Color.TRANSPARENT);

        // Add the body to the head
        addChild(rotationBody);
    }

    public abstract double getHeadHeight();
    public abstract double getHeadWidth();

}
