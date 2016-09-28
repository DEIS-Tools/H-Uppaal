package SW9.model_canvas.edges;

import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.shape.Line;
import javafx.scene.shape.Shape;

public class ArrowHead extends Parent {
    private final Line arrowHeadRight = new Line();
    private final Line arrowHeadLeft = new Line();
    public static final double LENGTH = 15;
    public static final double ANGLE = 15;

    public ArrowHead() {
        this.getChildren().add(arrowHeadRight);
        this.getChildren().add(arrowHeadLeft);
        arrowHeadRight.setMouseTransparent(true);
        arrowHeadLeft.setMouseTransparent(true);

    }

    public Line getArrowHeadRight() {
        return arrowHeadRight;
    }

    public Line getArrowHeadLeft() {
        return arrowHeadLeft;
    }

    // TODO This override mouse transparency

}
