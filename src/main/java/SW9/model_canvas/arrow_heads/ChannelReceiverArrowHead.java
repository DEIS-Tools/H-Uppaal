package SW9.model_canvas.arrow_heads;

import javafx.scene.shape.Circle;

public class ChannelReceiverArrowHead extends ArrowHead {

    private static final double CIRCLE_RADIUS = 10d;
    private final Circle circle;

    public ChannelReceiverArrowHead() {
        super();

        circle = initializeCircle();
        getChildren().add(circle);
    }

    private Circle initializeCircle() {
        final Circle circle = new Circle(CIRCLE_RADIUS);
        circle.getStyleClass().add("channel-receiver");

        circle.centerXProperty().bind(xProperty());
        circle.centerYProperty().bind(yProperty());

        return circle;
    }

    public Circle getCircle() {
        return circle;
    }

    @Override
    public double getHeadHeight() {
        return CIRCLE_RADIUS;
    }

    @Override
    public double getHeadWidth() {
        return CIRCLE_RADIUS;
    }

    @Override
    public void mark() {

    }

    @Override
    public void unmark() {

    }

    @Override
    public double getX() {
        return xProperty().get();
    }

    @Override
    public double getY() {
        return yProperty().get();
    }
}
