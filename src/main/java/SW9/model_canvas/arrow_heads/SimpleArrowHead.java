package SW9.model_canvas.arrow_heads;

import SW9.presentations.CanvasPresentation;
import javafx.scene.shape.Line;

public class SimpleArrowHead extends ArrowHead {

    private static final double TRIANGLE_LENGTH = CanvasPresentation.GRID_SIZE * 1.5;
    private static final double TRIANGLE_WIDTH = CanvasPresentation.GRID_SIZE;

    private Line leftArrow;
    private Line rightArrow;

    public SimpleArrowHead() {
        super();

        getChildren().addAll(initializeLeftArrow(), initializeRightArrow());
    }

    private Line initializeLeftArrow() {
        leftArrow = new Line();
        leftArrow.startXProperty().bind(xProperty());
        leftArrow.startYProperty().bind(yProperty());
        leftArrow.endXProperty().bind(xProperty().subtract(TRIANGLE_WIDTH / 2));
        leftArrow.endYProperty().bind(yProperty().subtract(TRIANGLE_LENGTH));

        leftArrow.getStyleClass().add("link");

        return leftArrow;
    }

    private Line initializeRightArrow() {
        rightArrow = new Line();
        rightArrow.startXProperty().bind(xProperty());
        rightArrow.startYProperty().bind(yProperty());
        rightArrow.endXProperty().bind(xProperty().add(TRIANGLE_WIDTH / 2));
        rightArrow.endYProperty().bind(yProperty().subtract(TRIANGLE_LENGTH));

        rightArrow.getStyleClass().add("link");

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

    @Override
    public void mark() {
        leftArrow.getStyleClass().add("selected");
        rightArrow.getStyleClass().add("selected");
    }

    @Override
    public void unmark() {
        leftArrow.getStyleClass().remove("selected");
        rightArrow.getStyleClass().remove("selected");
    }

}
