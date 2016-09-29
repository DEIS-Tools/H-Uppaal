package SW9.model_canvas.arrow_heads;

import SW9.model_canvas.IParent;
import SW9.model_canvas.Parent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Rectangle;


public abstract class ArrowHead extends Parent {
    public DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    public DoubleProperty yProperty = new SimpleDoubleProperty(0d);
    public Rectangle rotationBody = new Rectangle();

}
