package SW9.utility;

import SW9.model_canvas.Location;
import javafx.beans.binding.DoubleBinding;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.util.Pair;

public class BindingHelper {

    public static void bind(final Line subject, final Location source, final Location target) {
        // Calculate the bindings (so that the line will be based on the circle circumference instead of in its center)
        final Pair<DoubleBinding, DoubleBinding> startBindings = getCircleBindings(source, target);
        final Pair<DoubleBinding, DoubleBinding> endBindings = getCircleBindings(target, source);

        // Bind the subjects properties accordingly to our calculations
        subject.startXProperty().bind(startBindings.getKey());
        subject.startYProperty().bind(startBindings.getValue());
        subject.endXProperty().bind(endBindings.getKey());
        subject.endYProperty().bind(endBindings.getValue());
    }

    // Bindings for starting in a location
    private static Pair<DoubleBinding, DoubleBinding> getCircleBindings(final Circle source, final Circle target) {
        return new Pair<>(
                new DoubleBinding() {
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
                },
                new DoubleBinding() {
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
                }
        );

    }

}
