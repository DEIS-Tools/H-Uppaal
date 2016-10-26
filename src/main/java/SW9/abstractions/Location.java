package SW9.abstractions;

import SW9.utility.colors.Color;
import javafx.beans.property.*;

public class Location {

    // Verification properties
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty invariant = new SimpleStringProperty("data");
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(Type.NORMAL);
    private final ObjectProperty<Urgency> urgency = new SimpleObjectProperty<>(Urgency.COMMITTED);
    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I500);

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

    public enum Type {
        NORMAL, INITIAL, FINAl
    }

    public enum Urgency {
        NORMAL, URGENT, COMMITTED
    }

}
