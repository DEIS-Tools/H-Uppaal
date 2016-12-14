package SW9.abstractions;

import SW9.utility.helpers.Circular;
import SW9.utility.serialize.Serializable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.value.ObservableDoubleValue;

public class Nail implements Circular, Serializable {

    private static final String X = "x";
    private static final String Y = "y";
    private static final String PROPERTY_TYPE = "property_type";
    private static final String PROPERTY_X = "property_x";
    private static final String PROPERTY_Y = "property_y";

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty propertyX = new SimpleDoubleProperty(0d);
    private final DoubleProperty propertyY = new SimpleDoubleProperty(0d);
    private final DoubleProperty radius = new SimpleDoubleProperty(3d);
    private final ObjectProperty<Edge.PropertyType> propertyType = new SimpleObjectProperty<>(Edge.PropertyType.NONE);

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

    public DoubleProperty radiusProperty() {
        return radius;
    }

    public Edge.PropertyType getPropertyType() {
        return propertyType.get();
    }

    public ObjectProperty<Edge.PropertyType> propertyTypeProperty() {
        return propertyType;
    }

    public void setPropertyType(final Edge.PropertyType propertyType) {
        this.propertyType.set(propertyType);
    }

    public double getPropertyX() {
        return propertyX.get();
    }

    public DoubleProperty propertyXProperty() {
        return propertyX;
    }

    public void setPropertyX(final double propertyX) {
        this.propertyX.set(propertyX);
    }

    public double getPropertyY() {
        return propertyY.get();
    }

    public DoubleProperty propertyYProperty() {
        return propertyY;
    }

    public void setPropertyY(final double propertyY) {
        this.propertyY.set(propertyY);
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
        result.add(PROPERTY_TYPE, new Gson().toJsonTree(getPropertyType(), Edge.PropertyType.class));
        result.addProperty(PROPERTY_X, getPropertyX());
        result.addProperty(PROPERTY_Y, getPropertyY());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
        setPropertyType(new Gson().fromJson(json.getAsJsonPrimitive(PROPERTY_TYPE), Edge.PropertyType.class));
        setPropertyX(json.getAsJsonPrimitive(PROPERTY_X).getAsDouble());
        setPropertyY(json.getAsJsonPrimitive(PROPERTY_Y).getAsDouble());
    }

}
