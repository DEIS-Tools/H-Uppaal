package SW9.abstractions;

import SW9.utility.colors.Color;
import SW9.utility.helpers.Circular;
import SW9.utility.serialize.Serializable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.beans.property.*;

import java.util.concurrent.atomic.AtomicInteger;

public class Location implements Circular, Serializable {

    // Used to generate unique IDs
    private static final AtomicInteger hiddenID = new AtomicInteger(0);
    private static final String NAME = "name";
    private static final String INVARIANT = "invariant";
    private static final String TYPE = "type";
    private static final String URGENCY = "urgency";
    private static final String X = "x";
    private static final String Y = "Y";
    private static final String COLOR = "color";
    private static final String COLOR_INTENSITY = "colorIntensity";
    // Verification properties
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty invariant = new SimpleStringProperty("");
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(Type.NORMAL);
    private final ObjectProperty<Urgency> urgency = new SimpleObjectProperty<>(Urgency.NORMAL);
    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty radius = new SimpleDoubleProperty(0d);
    private final SimpleDoubleProperty scale = new SimpleDoubleProperty(1d);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I500);

    public Location() {
        setName("L" + hiddenID.getAndIncrement());
    }

    public Location(final JsonObject jsonObject) {
        hiddenID.incrementAndGet();
        deserialize(jsonObject);
    }

    public String getName() {
        return name.get();
    }

    public void setName(final String name) {
        this.name.set(name);
    }

    public StringProperty nameProperty() {
        return name;
    }

    public String getInvariant() {
        return invariant.get();
    }

    public void setInvariant(final String invariant) {
        this.invariant.set(invariant);
    }

    public StringProperty invariantProperty() {
        return invariant;
    }

    public Type getType() {
        return type.get();
    }

    public void setType(final Type type) {
        this.type.set(type);
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public Urgency getUrgency() {
        return urgency.get();
    }

    public void setUrgency(final Urgency urgency) {
        this.urgency.set(urgency);
    }

    public ObjectProperty<Urgency> urgencyProperty() {
        return urgency;
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

    public Color getColor() {
        return color.get();
    }

    public void setColor(final Color color) {
        this.color.set(color);
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public Color.Intensity getColorIntensity() {
        return colorIntensity.get();
    }

    public void setColorIntensity(final Color.Intensity colorIntensity) {
        this.colorIntensity.set(colorIntensity);
    }

    public ObjectProperty<Color.Intensity> colorIntensityProperty() {
        return colorIntensity;
    }

    /*
     * SERIALIZATION OF CLASS
     */

    public double getRadius() {
        return radius.get();
    }

    public void setRadius(final double radius) {
        this.radius.set(radius);
    }

    @Override
    public DoubleProperty radiusProperty() {
        return radius;
    }

    public double getScale() {
        return scale.get();
    }

    public void setScale(final double scale) {
        this.scale.set(scale);
    }

    @Override
    public DoubleProperty scaleProperty() {
        return scale;
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(NAME, getName());
        result.addProperty(INVARIANT, getInvariant());
        result.add(TYPE, new Gson().toJsonTree(getType(), Type.class));
        result.add(URGENCY, new Gson().toJsonTree(getUrgency(), Urgency.class));

        result.addProperty(X, getX());
        result.addProperty(Y, getY());
        result.addProperty(COLOR, "");
        result.addProperty(COLOR_INTENSITY, "");

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setName(json.getAsJsonPrimitive(NAME).getAsString());
        setInvariant(json.getAsJsonPrimitive(INVARIANT).getAsString());
        setType(new Gson().fromJson(json.getAsJsonPrimitive(TYPE), Type.class));
        setUrgency(new Gson().fromJson(json.getAsJsonPrimitive(URGENCY), Urgency.class));

        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
    }

    public enum Type {
        NORMAL, INITIAL, FINAl
    }

    public enum Urgency {
        NORMAL, URGENT, COMMITTED
    }

}
