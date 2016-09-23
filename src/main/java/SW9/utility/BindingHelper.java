package SW9.utility;

import SW9.MouseTracker;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;
import javafx.collections.ObservableList;
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

    public static void bind(final Circle subject, final MouseEvent target) {
        // Calculate the bindings (so that the subject will be centered on where the mouse event happened)
        subject.centerXProperty().bind(new SimpleDoubleProperty(target.getX()));
        subject.centerYProperty().bind(new SimpleDoubleProperty(target.getY()));
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
