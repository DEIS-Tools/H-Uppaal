package SW9.model_canvas.arrow_heads;

import javafx.scene.paint.Color;
import javafx.scene.shape.ArcTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;

public class BroadcastChannelSenderArrowHead extends ChannelSenderArrowHead {

    private static final double LARGE_CIRCLE_RADIUS = 15d;
    private static final double MEDIUM_CIRCLE_RADIUS = 10d;
    private static final double SMALL_CIRCLE_RADIUS = 5d;

    public BroadcastChannelSenderArrowHead() {
        super();

        getChildren().addAll(
                initializeLargeCircle(),
                initializeMediumCircle(),
                initializeSmallCircle()
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

    @Override
    public double getHeadHeight() {
        return LARGE_CIRCLE_RADIUS + super.getHeadHeight();
    }

    @Override
    public double getHeadWidth() {
        return Math.max(super.getHeadWidth(), LARGE_CIRCLE_RADIUS * 2);
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
