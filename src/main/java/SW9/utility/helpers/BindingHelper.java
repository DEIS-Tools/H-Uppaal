package SW9.utility.helpers;

import SW9.utility.mouse.MouseTracker;
import SW9.model_canvas.arrow_heads.ArrowHead;
import SW9.model_canvas.locations.Location;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import jiconfont.javafx.IconNode;

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

    public static void bind(final IconNode icon, final Location location) {
        icon.xProperty().bind(location.xProperty().add(Location.RADIUS));
        icon.yProperty().bind(location.yProperty().add(Location.RADIUS));
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

        private static LineBinding getCircleBindings(final Circle source, final Point target) {
            return new BindingHelper.LineBinding(
                    calculateXBinding(source, target),
                    calculateYBinding(source, target),
                    target.xProperty(),
                    target.yProperty()
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

        Point(final ArrowHead arrowHead) {
            this.x = arrowHead.xProperty;
            this.y = arrowHead.yProperty;
        }

        private ObservableDoubleValue xProperty() {
            return x;
        }

        private ObservableDoubleValue yProperty() {
            return y;
        }
    }

    public static void bind(final ArrowHead subject, final Circle source, final Circle target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircleBindings(source, target);

        ObservableDoubleValue startX = lineBinding.startX;
        ObservableDoubleValue startY = lineBinding.startY;
        ObservableDoubleValue endX = lineBinding.endX;
        ObservableDoubleValue endY = lineBinding.endY;

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

    public static void bind(final ArrowHead subject, final Circle source, final MouseTracker target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircleBindings(source, target);

        ObservableDoubleValue startX = lineBinding.startX;
        ObservableDoubleValue startY = lineBinding.startY;
        ObservableDoubleValue endX = lineBinding.endX;
        ObservableDoubleValue endY = lineBinding.endY;

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

    public static void bind(final Line subject, final ArrowHead target) {
        final Circle arrowHeadPrelimiters = new Circle();
        arrowHeadPrelimiters.centerXProperty().bind(target.xProperty);
        arrowHeadPrelimiters.centerYProperty().bind(target.yProperty);
        arrowHeadPrelimiters.setRadius(target.getHeadHeight());

        final LineBinding lineBinding = LineBinding.getCircleBindings(arrowHeadPrelimiters, new Point(subject.startXProperty(), subject.startYProperty()));

        if(target.shouldBindToTip()) {
            subject.endXProperty().bind(target.xProperty);
            subject.endYProperty().bind(target.yProperty);
        } else  {
            subject.endXProperty().bind(lineBinding.startX);
            subject.endYProperty().bind(lineBinding.startY);
        }
    }

}
