package SW9.utility.helpers;

import SW9.model_canvas.arrow_heads.ArrowHead;
import SW9.model_canvas.arrow_heads.ChannelReceiverArrowHead;
import SW9.model_canvas.arrow_heads.ChannelSenderArrowHead;
import SW9.presentations.CanvasPresentation;
import SW9.presentations.Link;
import SW9.presentations.TagPresentation;
import SW9.utility.mouse.MouseTracker;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.input.MouseEvent;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class BindingHelper {

    public static void bind(final Line subject, final TagPresentation target) {
        subject.startXProperty().set(0);
        subject.startYProperty().set(0);
        subject.endXProperty().bind(target.translateXProperty().add(target.minWidthProperty().divide(2)));
        subject.endYProperty().bind(target.translateYProperty().add(target.heightProperty().divide(2)));
        subject.opacityProperty().bind(target.opacityProperty());
        subject.visibleProperty().bind(target.visibleProperty());
    }

    public static void bind(final Circular subject, final ObservableDoubleValue x, final ObservableDoubleValue y) {
        subject.xProperty().bind(CanvasPresentation.mouseTracker.gridXProperty().subtract(x));
        subject.yProperty().bind(CanvasPresentation.mouseTracker.gridYProperty().subtract(y));
    }

    public static void bind(final Link lineSubject, final ArrowHead arrowHeadSubject, final Circular source, final Circular target) {
        BindingHelper.bind(lineSubject, source, target);
        BindingHelper.bind(arrowHeadSubject, source, target);
        BindingHelper.bind(lineSubject, arrowHeadSubject);
    }

    public static void bind(final Link lineSubject, final ArrowHead arrowHeadSubject, final Circular source, final ObservableDoubleValue x, final ObservableDoubleValue y) {
        BindingHelper.bind(lineSubject, source, x, y);
        BindingHelper.bind(arrowHeadSubject, source, x, y);
        BindingHelper.bind(lineSubject, arrowHeadSubject);
    }

    public static void bind(final Link subject, final Circle source, final Circle target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircleBindings(source, target);

        // Bind the subjects properties accordingly to our calculations
        subject.startXProperty().bind(lineBinding.startX);
        subject.startYProperty().bind(lineBinding.startY);
        subject.endXProperty().bind(lineBinding.endX);
        subject.endYProperty().bind(lineBinding.endY);
    }

    public static void bind(final Link subject, final Circle source, final MouseTracker target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircleBindings(source, target);

        // Bind the subjects properties accordingly to our calculations
        subject.startXProperty().bind(lineBinding.startX);
        subject.startYProperty().bind(lineBinding.startY);
        subject.endXProperty().bind(lineBinding.endX);
        subject.endYProperty().bind(lineBinding.endY);
    }

    public static void bind(final Link subject, final Circular source, final ObservableDoubleValue x, final ObservableDoubleValue y) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircularBindings(source, CanvasPresentation.mouseTracker, x, y);

        // Bind the subjects properties accordingly to our calculations
        subject.startXProperty().bind(lineBinding.startX);
        subject.startYProperty().bind(lineBinding.startY);
        subject.endXProperty().bind(lineBinding.endX);
        subject.endYProperty().bind(lineBinding.endY);
    }

    public static void bind(final Link subject, final Circular source, final Circular target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircularBindings(source, target);

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

    public static <T extends ChannelSenderArrowHead> void bind(final Link handshakeLine, final T senderArrowHead, final ChannelReceiverArrowHead receiverArrowHead) {
        final LineBinding bindings = LineBinding.getCircleBindings(receiverArrowHead.getCircle(), new Point(senderArrowHead));

        handshakeLine.startXProperty().bind(bindings.startX);
        handshakeLine.startYProperty().bind(bindings.startY);

        handshakeLine.endXProperty().bind(bindings.endX);
        handshakeLine.endYProperty().bind(bindings.endY);
    }

    public static void bind(final ArrowHead subject, final Circular source, final Circular target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircleBindings(source, target);

        ObservableDoubleValue startX = lineBinding.startX;
        ObservableDoubleValue startY = lineBinding.startY;
        ObservableDoubleValue endX = lineBinding.endX;
        ObservableDoubleValue endY = lineBinding.endY;

        subject.xProperty().bind(endX);
        subject.yProperty().bind(endY);

        DoubleBinding rotationBinding = new DoubleBinding() {
            {
                super.bind(startX, startY, endX, endY);
                super.bind(source.scaleProperty());
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(startY.get() - endY.get(), startX.get() - endX.get());
                return Math.toDegrees(angle) + 90;
            }
        };


        subject.rotateProperty().bind(rotationBinding);
    }

    public static void bind(final ArrowHead subject, final Circular source, final ObservableDoubleValue x, final ObservableDoubleValue y) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = LineBinding.getCircularBindings(source, CanvasPresentation.mouseTracker, x, y);

        ObservableDoubleValue startX = lineBinding.startX;
        ObservableDoubleValue startY = lineBinding.startY;
        ObservableDoubleValue endX = lineBinding.endX;
        ObservableDoubleValue endY = lineBinding.endY;

        subject.xProperty().bind(endX);
        subject.yProperty().bind(endY);

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

    public static void bind(final Link subject, final ArrowHead target) {
        final Circle arrowHeadField = new Circle();
        arrowHeadField.centerXProperty().bind(target.xProperty());
        arrowHeadField.centerYProperty().bind(target.yProperty());
        arrowHeadField.setRadius(target.getHeadHeight());

        final LineBinding lineBinding = LineBinding.getCircleBindings(arrowHeadField, new Point(subject.startXProperty(), subject.startYProperty()));

        if (target.shouldBindToTip()) {
            subject.endXProperty().bind(target.xProperty());
            subject.endYProperty().bind(target.yProperty());
        } else {
            subject.endXProperty().bind(lineBinding.startX);
            subject.endYProperty().bind(lineBinding.startY);
        }
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
                    target.xProperty(),
                    target.yProperty()
            );
        }

        private static <T extends Circular> LineBinding getCircleBindings(final T source, final T target) {
            return new BindingHelper.LineBinding(
                    calculateXBinding(source, new Point(target)),
                    calculateYBinding(source, new Point(target)),
                    calculateXBinding(target, new Point(source)),
                    calculateYBinding(target, new Point(source))
            );
        }

        private static LineBinding getCircularBindings(final Circular source, final MouseTracker target, final ObservableDoubleValue x, final ObservableDoubleValue y) {
            final ObservableDoubleValue mouseX = target.gridXProperty().subtract(x);
            final ObservableDoubleValue mouseY = target.gridYProperty().subtract(y);

            final Point point = new Point(mouseX, mouseY);

            return new BindingHelper.LineBinding(
                    calculateXBinding(source, point),
                    calculateYBinding(source, point),
                    mouseX,
                    mouseY
            );
        }

        private static LineBinding getCircularBindings(final Circular source, final Circular target) {
            final Point sourcePoint = new Point(source.xProperty(), source.yProperty());
            final Point targetPoint = new Point(target.xProperty(), target.yProperty());

            return new BindingHelper.LineBinding(
                    calculateXBinding(source, targetPoint),
                    calculateYBinding(source, targetPoint),
                    calculateXBinding(target, sourcePoint),
                    calculateYBinding(target, sourcePoint)
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

        private static <T extends Circular> ObservableDoubleValue calculateXBinding(final T source, final Point target) {
            return new DoubleBinding() {
                {
                    super.bind(source.xProperty(), source.yProperty());
                    super.bind(target.xProperty(), target.yProperty());
                    super.bind(source.radiusProperty());
                    super.bind(source.scaleProperty());
                }

                @Override
                protected double computeValue() {
                    final double angle = Math.atan2(source.yProperty().get() - target.yProperty().get(), source.xProperty().get() - target.xProperty().get()) - Math.toRadians(180);
                    return source.xProperty().get() + source.radiusProperty().get() * source.scaleProperty().get() * Math.cos(angle);
                }
            };
        }

        private static <T extends Circular> ObservableDoubleValue calculateYBinding(final T source, final Point target) {
            return new DoubleBinding() {
                {
                    super.bind(source.xProperty(), source.yProperty());
                    super.bind(target.xProperty(), target.yProperty());
                    super.bind(source.radiusProperty());
                    super.bind(source.scaleProperty());
                }

                @Override
                protected double computeValue() {
                    final double angle = Math.atan2(source.yProperty().get() - target.yProperty().get(), source.xProperty().get() - target.xProperty().get()) - Math.toRadians(180);
                    return source.yProperty().get() + source.radiusProperty().get() * source.scaleProperty().get() * Math.sin(angle);
                }
            };
        }
    }

    private static class Point {
        private final ObservableDoubleValue x, y;

        Point(final Circular circular) {
            this.x = circular.xProperty();
            this.y = circular.yProperty();
        }

        Point(final ObservableDoubleValue x, final ObservableDoubleValue y) {
            this.x = x;
            this.y = y;
        }

        Point(final Circle circle) {
            this.x = circle.centerXProperty();
            this.y = circle.centerYProperty();
        }

        Point(final MouseTracker mouseTracker) {
            this.x = mouseTracker.xProperty();
            this.y = mouseTracker.yProperty();
        }

        Point(final ArrowHead arrowHead) {
            this.x = arrowHead.xProperty();
            this.y = arrowHead.yProperty();
        }

        private ObservableDoubleValue xProperty() {
            return x;
        }

        private ObservableDoubleValue yProperty() {
            return y;
        }
    }

}
