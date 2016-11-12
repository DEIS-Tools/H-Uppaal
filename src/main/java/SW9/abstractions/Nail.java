package SW9.abstractions;

import SW9.utility.helpers.Circular;
import SW9.utility.serialize.Serializable;
import com.google.gson.JsonObject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ObservableDoubleValue;

public class Nail implements Circular, Serializable {

    private static final String X = "x";
    private static final String Y = "Y";
    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty radius = new SimpleDoubleProperty(3d);

    public Nail(final ObservableDoubleValue x, final ObservableDoubleValue y) {
        this(x.get(), y.get());
    }

    public Nail(final double x, final double y) {
        this.x.setValue(x);
        this.y.setValue(y);
    }

    public Nail(final JsonObject jsonObject) {
        deserialize(jsonObject);
    }

    public double getX() {
        return x.get();
    }

    public void setX(final double x) {
        this.x.set(x);
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public double getY() {
        return y.get();
    }

    public void setY(final double y) {
        this.y.set(y);
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double getRadius() {
        return radius.get();
    }

    public void setRadius(final double radius) {
        this.radius.set(radius);
    }

    /*
     * SERIALIZATION OF CLASS
     */

    public DoubleProperty radiusProperty() {
        return radius;
    }

    @Override
    public DoubleProperty scaleProperty() {
        return new SimpleDoubleProperty(1d);
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(X, getX());
        result.addProperty(Y, getY());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
    }
}
