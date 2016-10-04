package SW9.model_canvas.arrow_heads;

import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class HandshakeArrowHead extends ArrowHead {

    private static final double CIRCLE_RADIUS = 15d;
    private static final double TRIANGLE_LENGTH = 15d;
    private static final double TRIANGLE_WIDTH = 15d;

    public HandshakeArrowHead() {
        super();

        addChildren(initializeHalfCircle(), initializeTriangle());
    }

    private Path initializeHalfCircle() {
        final Path halfCircle = new Path();

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

        return halfCircle;
    }

    private Path initializeTriangle() {
        final Path triangle = new Path();

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

        return triangle;
    }

    @Override
    public double getHeadHeight() {
        return CIRCLE_RADIUS + TRIANGLE_LENGTH;
    }

    @Override
    public double getHeadWidth() {
        return Math.max(TRIANGLE_WIDTH, CIRCLE_RADIUS * 2);
    }

    @Override
    public void mark() {
        // TODO: Not implemented yet
    }

    @Override
    public void unmark() {
        // TODO: Not implemented yet
    }

}
