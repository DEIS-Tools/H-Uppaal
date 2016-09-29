package SW9.model_canvas.arrow_heads;

import SW9.model_canvas.Parent;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class ChannelHandshake extends Arrow {
    private Path halfCircle;
    private Path triangle;

    private static final double CIRCLE_RADIUS = 15d;
    private static final double TRIANGLE_LENGTH = 15d;
    private static final double TRIANGLE_WIDTH = 15d;

    @Override
    protected void initializeHead(Parent head) {
        initializeHalfCircle();
        initializeTriangle();
        head.addChildren(halfCircle, triangle);
    }

    @Override
    public double getHeadHeight() {
        return CIRCLE_RADIUS + TRIANGLE_LENGTH;
    }

    @Override
    public double getHeadWidth() {
        return Math.max(TRIANGLE_WIDTH, CIRCLE_RADIUS * 2);
    }

    private void initializeHalfCircle() {
        halfCircle = new Path();
        halfCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty.add(CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty);

        p2.xProperty().bind(xProperty.subtract(CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty);
        p2.setRadiusX(CIRCLE_RADIUS);
        p2.setRadiusY(CIRCLE_RADIUS);

        halfCircle.getElements().add(p1);
        halfCircle.getElements().add(p2);
    }

    private void initializeTriangle() {
        triangle = new Path();
        MoveTo start = new MoveTo();
        LineTo l1 = new LineTo();
        LineTo l2 = new LineTo();
        LineTo l3 = new LineTo();

        start.xProperty().bind(xProperty);
        start.yProperty().bind(yProperty.subtract(CIRCLE_RADIUS));

        l1.xProperty().bind(start.xProperty().subtract(TRIANGLE_WIDTH / 2));
        l1.yProperty().bind(start.yProperty().subtract(TRIANGLE_LENGTH));

        l2.xProperty().bind(start.xProperty().add(TRIANGLE_WIDTH / 2));
        l2.yProperty().bind(start.yProperty().subtract(TRIANGLE_LENGTH));

        l3.xProperty().bind(start.xProperty());
        l3.yProperty().bind(start.yProperty());

        triangle.setFill(Color.BLACK);
        triangle.getElements().addAll(start, l1, l2, l3);
    }

}
