package SW9.model_canvas.arrow_heads;

import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class ChannelBroadcastHead extends ArrowHead {
    private Path largeCircle = new Path();
    private Path mediumCircle = new Path();
    private Path smallCircle = new Path();
    private Path triangle = new Path();

    private static final double LARGE_CIRCLE_RADIUS = 15d;
    private static final double MEDIUM_CIRCLE_RADIUS = 10d;
    private static final double SMALL_CIRCLE_RADIUS = 5d;
    private static final double TRIANGLE_LENGTH = 15d;
    private static final double TRIANGLE_WIDTH = 15d;

    public ChannelBroadcastHead() {
        initializeRotationBody();
        initializeLargeCircle();
        initializeMediumCircle();
        initializeSmallCircle();
        initializeTriangle();

        addChildren(largeCircle, mediumCircle, smallCircle, triangle, rotationBody);
    }

    protected void initializeRotationBody() {
        rotationBody.xProperty().bind(xProperty.subtract(TRIANGLE_WIDTH / 2));
        rotationBody.yProperty().bind(yProperty.subtract(LARGE_CIRCLE_RADIUS + TRIANGLE_LENGTH));
        rotationBody.widthProperty().set(TRIANGLE_WIDTH);
        rotationBody.heightProperty().set((LARGE_CIRCLE_RADIUS + TRIANGLE_LENGTH) * 2);
        rotationBody.setFill(Color.TRANSPARENT);
        rotationBody.setMouseTransparent(true);
    }

    private void initializeLargeCircle() {
        largeCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty.add(LARGE_CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty);

        p2.xProperty().bind(xProperty.subtract(LARGE_CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty);
        p2.setRadiusX(LARGE_CIRCLE_RADIUS);
        p2.setRadiusY(LARGE_CIRCLE_RADIUS);

        largeCircle.getElements().add(p1);
        largeCircle.getElements().add(p2);
    }

    private void initializeMediumCircle() {
        mediumCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty.add(MEDIUM_CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty);

        p2.xProperty().bind(xProperty.subtract(MEDIUM_CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty);
        p2.setRadiusX(MEDIUM_CIRCLE_RADIUS);
        p2.setRadiusY(MEDIUM_CIRCLE_RADIUS);

        mediumCircle.getElements().add(p1);
        mediumCircle.getElements().add(p2);
    }

    private void initializeSmallCircle() {
        smallCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty.add(SMALL_CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty);

        p2.xProperty().bind(xProperty.subtract(SMALL_CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty);
        p2.setRadiusX(SMALL_CIRCLE_RADIUS);
        p2.setRadiusY(SMALL_CIRCLE_RADIUS);

        smallCircle.getElements().add(p1);
        smallCircle.getElements().add(p2);
    }

    private void initializeTriangle() {
        MoveTo start = new MoveTo();
        LineTo l1 = new LineTo();
        LineTo l2 = new LineTo();
        LineTo l3 = new LineTo();

        start.xProperty().bind(xProperty);
        start.yProperty().bind(yProperty.subtract(LARGE_CIRCLE_RADIUS));

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
