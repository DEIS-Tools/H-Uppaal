package SW9.model_canvas.arrow_heads;

import javafx.scene.paint.Color;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public abstract class ChannelSenderArrowHead extends ArrowHead {

    private static final double TRIANGLE_LENGTH = 15d;
    private static final double TRIANGLE_WIDTH = 15d;

    public ChannelSenderArrowHead() {
        super();
        addChild(initializeTriangle());
    }
    private Path initializeTriangle() {
        final Path triangle = new Path();

        MoveTo start = new MoveTo();
        LineTo l1 = new LineTo();
        LineTo l2 = new LineTo();
        LineTo l3 = new LineTo();

        start.xProperty().bind(xProperty());
        start.yProperty().bind(yProperty().subtract(getHeadHeight() - TRIANGLE_LENGTH));

        l1.xProperty().bind(start.xProperty().subtract(TRIANGLE_WIDTH / 2));
        l1.yProperty().bind(start.yProperty().subtract(TRIANGLE_LENGTH));

        l2.xProperty().bind(start.xProperty().add(TRIANGLE_WIDTH / 2));
        l2.yProperty().bind(start.yProperty().subtract(TRIANGLE_LENGTH));

        l3.xProperty().bind(start.xProperty());
        l3.yProperty().bind(start.yProperty());

        triangle.setFill(Color.BLACK);
        triangle.getElements().addAll(start, l1, l2, l3);

        return triangle;
    }

    @Override
    public double getHeadHeight() {
        return TRIANGLE_LENGTH;
    }

    @Override
    public double getHeadWidth() {
        return TRIANGLE_WIDTH;
    }
}
