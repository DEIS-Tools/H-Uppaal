package SW9.model_canvas.edges;

import SW9.model_canvas.Parent;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.paint.Color;
import javafx.scene.shape.*;

public class ChannelHandshakeHead extends Parent {

    public DoubleProperty xProperty = new SimpleDoubleProperty(0d);
    public DoubleProperty yProperty = new SimpleDoubleProperty(0d);;

    private Rectangle rotationBody = new Rectangle();
    private Path halfCircle = new Path();
    private Path triangle = new Path();

    private static final double HALF_CIRCLE_RADIUS = 25d;
    private static final double TRIANGLE_LENGTH = 25d;
    private static final double TRIANGLE_WIDTH = 25d;

    public ChannelHandshakeHead() {
        initializeRotationBody();
        initializeHalfCircle();
        initializeTriangle();

        addChildren(halfCircle, triangle, rotationBody);
    }

    private void initializeRotationBody() {
        rotationBody.xProperty().bind(xProperty.subtract(TRIANGLE_WIDTH / 2));
        rotationBody.yProperty().bind(yProperty.subtract(HALF_CIRCLE_RADIUS + TRIANGLE_LENGTH));
        rotationBody.widthProperty().set(TRIANGLE_WIDTH);
        rotationBody.heightProperty().set((HALF_CIRCLE_RADIUS + TRIANGLE_LENGTH) *2 );
        rotationBody.setFill(Color.TRANSPARENT);
        rotationBody.setMouseTransparent(true);
    }
    private void initializeHalfCircle() {
        halfCircle.setFill(Color.TRANSPARENT);
        halfCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty.add(HALF_CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty);

        p2.xProperty().bind(xProperty.subtract(HALF_CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty);
        p2.setRadiusX(HALF_CIRCLE_RADIUS);
        p2.setRadiusY(HALF_CIRCLE_RADIUS);

        halfCircle.getElements().add(p1);
        halfCircle.getElements().add(p2);
    }
    private void initializeTriangle() {
        MoveTo start = new MoveTo();
        LineTo l1 = new LineTo();
        LineTo l2 = new LineTo();
        LineTo l3 = new LineTo();

        start.xProperty().bind(xProperty);
        start.yProperty().bind(yProperty.subtract(HALF_CIRCLE_RADIUS));

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
