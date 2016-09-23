package SW9.utility;

import SW9.model_canvas.Location;
import javafx.beans.binding.DoubleBinding;
import javafx.beans.value.ObservableDoubleValue;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;

public class BindingHelper {

    public static void bind(final Line subject, final Location source, final Location target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final LineBinding lineBinding = getCircleBindings(source, target);

        // Bind the subjects properties accordingly to our calculations
        subject.startXProperty().bind(lineBinding.startX);
        subject.startYProperty().bind(lineBinding.startY);
        subject.endXProperty().bind(lineBinding.endX);
        subject.endYProperty().bind(lineBinding.endY);
    }

    // Bindings for starting in a location
    private static LineBinding getCircleBindings(final Circle source, final Circle target) {
        return new BindingHelper.LineBinding(
                calculateCircleXLineBinding(source, target),
                calculateCircleYLineBinding(source, target),
                calculateCircleXLineBinding(target, source),
                calculateCircleYLineBinding(target, source));
    }

    private static ObservableDoubleValue calculateCircleXLineBinding(final Circle source, final Circle target) {
        return new DoubleBinding() {
            {
                super.bind(source.centerXProperty(), source.centerYProperty());
                super.bind(target.centerXProperty(), target.centerYProperty());
                super.bind(source.radiusProperty());
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(source.centerYProperty().get() - target.centerYProperty().get(), source.centerXProperty().get() - target.centerXProperty().get()) - Math.toRadians(180);
                return source.centerXProperty().get() + source.radiusProperty().get() * Math.cos(angle);
            }
        };
    }

    private static ObservableDoubleValue calculateCircleYLineBinding(final Circle source, final Circle target) {
        return new DoubleBinding() {
            {
                super.bind(source.centerXProperty(), source.centerYProperty());
                super.bind(target.centerXProperty(), target.centerYProperty());
                super.bind(source.radiusProperty());
            }

            @Override
            protected double computeValue() {
                double angle = Math.atan2(source.centerYProperty().get() - target.centerYProperty().get(), source.centerXProperty().get() - target.centerXProperty().get()) - Math.toRadians(180);
                return source.centerYProperty().get() + source.radiusProperty().get() * Math.sin(angle);
            }
        };
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
    }

}
