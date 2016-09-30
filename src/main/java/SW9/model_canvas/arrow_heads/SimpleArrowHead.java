package SW9.model_canvas.arrow_heads;

import javafx.scene.shape.Line;

public class SimpleArrowHead extends ArrowHead {

    private static final double TRIANGLE_LENGTH = 20d;
    private static final double TRIANGLE_WIDTH = 15d;

    public SimpleArrowHead() {
        super();

        addChildren(initializeLeftArrow(), initializeRightArrow());
    }

    private Line initializeLeftArrow() {
        final Line leftArrow = new Line();
        leftArrow.startXProperty().bind(xProperty);
        leftArrow.startYProperty().bind(yProperty);
        leftArrow.endXProperty().bind(xProperty.subtract(TRIANGLE_WIDTH / 2));
        leftArrow.endYProperty().bind(yProperty.subtract(TRIANGLE_LENGTH));

        return leftArrow;
    }

    private Line initializeRightArrow() {
        final Line rightArrow = new Line();
        rightArrow.startXProperty().bind(xProperty);
        rightArrow.startYProperty().bind(yProperty);
        rightArrow.endXProperty().bind(xProperty.add(TRIANGLE_WIDTH / 2));
        rightArrow.endYProperty().bind(yProperty.subtract(TRIANGLE_LENGTH));

        return rightArrow;
    }

    @Override
    public double getHeadHeight() {
        return TRIANGLE_LENGTH;
    }

    @Override
    public double getHeadWidth() {
        return TRIANGLE_WIDTH;
    }

    @Override
    public boolean shouldBindToTip() {
        return true;
    }

}
