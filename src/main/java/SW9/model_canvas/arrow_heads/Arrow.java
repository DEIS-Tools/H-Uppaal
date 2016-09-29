package SW9.model_canvas.arrow_heads;

import SW9.model_canvas.Parent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


public abstract class Arrow extends Parent {
    public final DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    public final DoubleProperty yProperty = new SimpleDoubleProperty(0d);
    private final Parent head = new Parent();
    private final Rectangle rotationBody = new Rectangle();
    private final Line tail = new Line();

    public Arrow() {
        initializeHead(head);

        // Calculate the rotationbody
        rotationBody.xProperty().bind(xProperty.subtract(getHeadWidth() / 2));
        rotationBody.yProperty().bind(yProperty.subtract(getHeadHeight()));
        rotationBody.widthProperty().set(getHeadWidth());
        rotationBody.heightProperty().set(getHeadHeight() * 2);
        rotationBody.setMouseTransparent(true);
        rotationBody.setFill(Color.TRANSPARENT);

        // Add the body to the head
        head.addChild(rotationBody);

        // Add the head to the arrow
        addChild(head);
        addChild(tail);
    }

    public abstract double getHeadHeight();
    public abstract double getHeadWidth();
    protected abstract void initializeHead(final Parent head);

    public final Line getTail() {
        return tail;
    }

    public final Parent getHead() {
        return head;
    }

}
