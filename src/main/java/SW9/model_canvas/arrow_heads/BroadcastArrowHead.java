package SW9.model_canvas.arrow_heads;

import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.LineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class BroadcastArrowHead extends ArrowHead {

    private static final double LARGE_CIRCLE_RADIUS = 15d;
    private static final double MEDIUM_CIRCLE_RADIUS = 10d;
    private static final double SMALL_CIRCLE_RADIUS = 5d;
    private static final double TRIANGLE_LENGTH = 15d;
    private static final double TRIANGLE_WIDTH = 15d;

    public BroadcastArrowHead() {
        super();

        addChildren(
                initializeLargeCircle(),
                initializeMediumCircle(),
                initializeSmallCircle(),
                initializeTriangle()
        );
    }

    private Path initializeLargeCircle() {
        final Path largeCircle = new Path();

        largeCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty().add(LARGE_CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty());

        p2.xProperty().bind(xProperty().subtract(LARGE_CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty());
        p2.setRadiusX(LARGE_CIRCLE_RADIUS);
        p2.setRadiusY(LARGE_CIRCLE_RADIUS);

        largeCircle.getElements().add(p1);
        largeCircle.getElements().add(p2);

        return largeCircle;
    }


    private Path initializeMediumCircle() {
        final Path mediumCircle = new Path();

        mediumCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty().add(MEDIUM_CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty());

        p2.xProperty().bind(xProperty().subtract(MEDIUM_CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty());
        p2.setRadiusX(MEDIUM_CIRCLE_RADIUS);
        p2.setRadiusY(MEDIUM_CIRCLE_RADIUS);

        mediumCircle.getElements().add(p1);
        mediumCircle.getElements().add(p2);

        return mediumCircle;
    }

    private Path initializeSmallCircle() {
        final Path smallCircle = new Path();

        smallCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty().add(SMALL_CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty());

        p2.xProperty().bind(xProperty().subtract(SMALL_CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty());
        p2.setRadiusX(SMALL_CIRCLE_RADIUS);
        p2.setRadiusY(SMALL_CIRCLE_RADIUS);

        smallCircle.getElements().add(p1);
        smallCircle.getElements().add(p2);

        return smallCircle;
    }

    private Path initializeTriangle() {
        final Path triangle = new Path();

        MoveTo start = new MoveTo();
        LineTo l1 = new LineTo();
        LineTo l2 = new LineTo();
        LineTo l3 = new LineTo();

        start.xProperty().bind(xProperty());
        start.yProperty().bind(yProperty().subtract(LARGE_CIRCLE_RADIUS));

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
        return LARGE_CIRCLE_RADIUS + TRIANGLE_LENGTH;
    }

    @Override
    public double getHeadWidth() {
        return Math.max(TRIANGLE_WIDTH, LARGE_CIRCLE_RADIUS * 2);
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
