package SW9.utility;

import SW9.MouseTracker;
import SW9.model_canvas.edges.ArrowHead;
import SW9.model_canvas.edges.ChannelHandshakeHead;
import SW9.model_canvas.locations.Location;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class BindingHelper {

    public static void bind(final Line subject, final Circle source, final Circle target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircleBindings(source, target);

        // Bind the subjects properties accordingly to our calculations
        subject.startXProperty().bind(lineBinding.startX);
        subject.startYProperty().bind(lineBinding.startY);
        subject.endXProperty().bind(lineBinding.endX);
        subject.endYProperty().bind(lineBinding.endY);
    }

    public static void bind(final Line subject, final Circle source, final MouseTracker target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircleBindings(source, target);

        // Bind the subjects properties accordingly to our calculations
        subject.startXProperty().bind(lineBinding.startX);
        subject.startYProperty().bind(lineBinding.startY);
        subject.endXProperty().bind(lineBinding.endX);
        subject.endYProperty().bind(lineBinding.endY);
    }

    public static void place(final Circle subject, final MouseEvent target) {
        // Calculate the bindings (so that the subject will be centered on where the mouse event happened)
        subject.centerXProperty().set(target.getX());
        subject.centerYProperty().set(target.getY());
    }

    public static void bind(final ArrowHead subject, final Line source) {
        // Calculate the bindings (so that the arrow head is bound to the end of the line)

        DoubleProperty startX = source.startXProperty();
        DoubleProperty startY = source.startYProperty();
        DoubleProperty endX = source.endXProperty();
        DoubleProperty endY = source.endYProperty();

        DoubleBinding arrowHeadLeftX = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) + Math.toRadians(ArrowHead.ANGLE);
                return endX.get() + Math.cos(angle) * ArrowHead.LENGTH;
            }
        };

        DoubleBinding arrowHeadLeftY = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) + Math.toRadians(ArrowHead.ANGLE);
                return endY.get() + Math.sin(angle) * ArrowHead.LENGTH;
            }
        };

        DoubleBinding arrowHeadRightX = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) - Math.toRadians(ArrowHead.ANGLE);
                return endX.get() + Math.cos(angle) * ArrowHead.LENGTH;
            }
        };

        DoubleBinding arrowHeadRightY = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get()) - Math.toRadians(ArrowHead.ANGLE);
                return endY.get() + Math.sin(angle) * ArrowHead.LENGTH;
            }
        };

        subject.getArrowHeadLeft().startXProperty().bind(arrowHeadLeftX);
        subject.getArrowHeadLeft().startYProperty().bind(arrowHeadLeftY);
        subject.getArrowHeadLeft().endXProperty().bind(endX);
        subject.getArrowHeadLeft().endYProperty().bind(endY);

        subject.getArrowHeadRight().startXProperty().bind(arrowHeadRightX);
        subject.getArrowHeadRight().startYProperty().bind(arrowHeadRightY);
        subject.getArrowHeadRight().endXProperty().bind(endX);
        subject.getArrowHeadRight().endYProperty().bind(endY);
    }

    public static void bind(final ChannelHandshakeHead subject, final Line source) {
        DoubleProperty startX = source.startXProperty();
        DoubleProperty startY = source.startYProperty();
        DoubleProperty endX = source.endXProperty();
        DoubleProperty endY = source.endYProperty();

        subject.xProperty.bind(endX);
        subject.yProperty.bind(endY);

        DoubleBinding rotationBinding = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get());
                return Math.toDegrees(angle) + 90;
            }
        };

        subject.rotateProperty().bind(rotationBinding);
    }

    public static void bind(final Label subject, final Location target) {
        subject.layoutXProperty().bind(new DoubleBinding() {
            {
                super.bind(subject.textProperty(), target.circle.centerXProperty());
            }
            @Override
            protected double computeValue() {
                return target.circle.getCenterX() - Location.RADIUS;
            }
        });

        subject.layoutYProperty().bind(new DoubleBinding() {
            {
                super.bind(subject.textProperty(), target.circle.centerYProperty());
            }
            @Override
            protected double computeValue() {
                return target.circle.getCenterY() - Location.RADIUS;
            }
        });
    }

    private static class LineBinding {
    final ObservableDoubleValue startX;
    final ObservableDoubleValue startY;
    final ObservableDoubleValue endX;
    final ObservableDoubleValue endY;

    LineBinding(final ObservableDoubleValue startX,
                final ObservableDoubleValue startY,
                final ObservableDoubleValue endX,
                final ObservableDoubleValue endY) {
        this.startX = startX;
        this.startY = startY;
        this.endX = endX;
        this.endY = endY;
    }

    // Bindings for starting in a location
    private static LineBinding getCircleBindings(final Circle source, final Circle target) {
        return new BindingHelper.LineBinding(
                calculateXBinding(source, new Point(target)),
                calculateYBinding(source, new Point(target)),
                calculateXBinding(target, new Point(source)),
                calculateYBinding(target, new Point(source))
        );
    }

    /**
     * Calculate the bindings for starting from a circle and ending on the mouse
     */
    private static LineBinding getCircleBindings(final Circle source, final MouseTracker target) {
        return new BindingHelper.LineBinding(
                calculateXBinding(source, new Point(target)),
                calculateYBinding(source, new Point(target)),
                target.getXProperty(),
                target.getYProperty()
        );
    }

    private static ObservableDoubleValue calculateXBinding(final Circle source, final Point target) {
        return new DoubleBinding() {
            {
                super.bind(source.centerXProperty(), source.centerYProperty());
                super.bind(target.xProperty(), target.yProperty());
                super.bind(source.radiusProperty());
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(source.centerYProperty().get() - target.yProperty().get(), source.centerXProperty().get() - target.xProperty().get()) - Math.toRadians(180);
                return source.centerXProperty().get() + source.radiusProperty().get() * Math.cos(angle);
            }
        };
    }

    private static ObservableDoubleValue calculateYBinding(final Circle source, final Point target) {
        return new DoubleBinding() {
            {
                super.bind(source.centerXProperty(), source.centerYProperty());
                super.bind(target.xProperty(), target.yProperty());
                super.bind(source.radiusProperty());
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(source.centerYProperty().get() - target.yProperty().get(), source.centerXProperty().get() - target.xProperty().get()) - Math.toRadians(180);
                return source.centerYProperty().get() + source.radiusProperty().get() * Math.sin(angle);
            }
        };
    }
}

private static class Point {
    private ObservableDoubleValue x, y;

    Point(final ObservableDoubleValue x, final ObservableDoubleValue y) {
        this.x = x;
        this.y = y;
    }

    Point(final Circle circle) {
        this.x = circle.centerXProperty();
        this.y = circle.centerYProperty();
    }

    Point(final MouseTracker mouseTracker) {
        this.x = mouseTracker.getXProperty();
        this.y = mouseTracker.getYProperty();
    }

    private ObservableDoubleValue xProperty() {
        return x;
    }

    private ObservableDoubleValue yProperty() {
        return y;
    }
}

}
