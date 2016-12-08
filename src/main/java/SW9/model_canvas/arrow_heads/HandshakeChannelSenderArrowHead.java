package SW9.model_canvas.arrow_heads;

import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class HandshakeChannelSenderArrowHead extends ChannelSenderArrowHead {

    protected static final double CIRCLE_RADIUS = 15d;

    public HandshakeChannelSenderArrowHead() {
        super();

        getChildren().add(initializeHalfCircle());
    }

    private Path initializeHalfCircle() {
        final Path halfCircle = new Path();

        halfCircle.setStroke(Color.BLACK);
        MoveTo p1 = new MoveTo();
        ArcTo p2 = new ArcTo();

        p1.xProperty().bind(xProperty().add(CIRCLE_RADIUS));
        p1.yProperty().bind(yProperty());

        p2.xProperty().bind(xProperty().subtract(CIRCLE_RADIUS));
        p2.yProperty().bind(yProperty());
        p2.setRadiusX(CIRCLE_RADIUS);
        p2.setRadiusY(CIRCLE_RADIUS);

        halfCircle.getElements().add(p1);
        halfCircle.getElements().add(p2);

        return halfCircle;
    }

    @Override
    public double getHeadHeight() {
        return super.getHeadHeight() + CIRCLE_RADIUS;
    }

    @Override
    public double getHeadWidth() {
        return Math.max(super.getHeadHeight(), CIRCLE_RADIUS * 2);
    }


    @Override
    public void mark() {
        // TODO: Not implemented yet
    }

    @Override
    public void unmark() {
        // TODO: Not implemented yet
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
