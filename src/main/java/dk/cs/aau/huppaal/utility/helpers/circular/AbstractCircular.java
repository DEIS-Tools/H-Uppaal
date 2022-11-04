package dk.cs.aau.huppaal.utility.helpers.circular;

import dk.cs.aau.huppaal.utility.helpers.Circular;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;

public abstract class AbstractCircular implements Circular {
    protected final DoubleProperty x;
    protected final DoubleProperty y;
    protected final DoubleProperty radius;
    protected final DoubleProperty scale;

    public AbstractCircular(DoubleProperty x, DoubleProperty y, DoubleProperty radius, DoubleProperty scale) {
        this.x = x;
        this.y = y;
        this.radius = radius;
        this.scale = scale;
    }

    public AbstractCircular(double x, double y, double radius, double scale) {
        this.x = new SimpleDoubleProperty(x);
        this.y = new SimpleDoubleProperty(y);
        this.radius = new SimpleDoubleProperty(radius);
        this.scale = new SimpleDoubleProperty(scale);
    }

    @Override
    public DoubleProperty radiusProperty() {
        return radius;
    }

    @Override
    public DoubleProperty scaleProperty() {
        return scale;
    }

    @Override
    public DoubleProperty xProperty() {
        return x;
    }

    @Override
    public DoubleProperty yProperty() {
        return y;
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
