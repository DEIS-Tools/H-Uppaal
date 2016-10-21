package SW9.abstractions.components;

import SW9.utility.colors.Color;
import javafx.beans.property.*;

public class Location {

    public enum Type {
        NORMAL, INITIAL, FINAl
    }

    public enum Urgency {
        NORMAL, URGENT, COMMITTED
    }

    // Verification properties
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty invariant = new SimpleStringProperty("");
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(Type.NORMAL);
    private final ObjectProperty<Urgency> urgency = new SimpleObjectProperty<>(Urgency.NORMAL);

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final ObjectProperty<Color> color = new SimpleObjectProperty<>(Color.GREY_BLUE);
    private final ObjectProperty<Color.Intensity> colorIntensity = new SimpleObjectProperty<>(Color.Intensity.I700);

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(final String name) {
        this.name.set(name);
    }

    public String getInvariant() {
        return invariant.get();
    }

    public StringProperty invariantProperty() {
        return invariant;
    }

    public void setInvariant(final String invariant) {
        this.invariant.set(invariant);
    }

    public Type getType() {
        return type.get();
    }

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public void setType(final Type type) {
        this.type.set(type);
    }

    public Urgency getUrgency() {
        return urgency.get();
    }

    public ObjectProperty<Urgency> urgencyProperty() {
        return urgency;
    }

    public void setUrgency(final Urgency urgency) {
        this.urgency.set(urgency);
    }

    public double getX() {
        return x.get();
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public void setX(final double x) {
        this.x.set(x);
    }

    public double getY() {
        return y.get();
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public void setY(final double y) {
        this.y.set(y);
    }

    public Color getColor() {
        return color.get();
    }

    public ObjectProperty<Color> colorProperty() {
        return color;
    }

    public void setColor(final Color color) {
        this.color.set(color);
    }

    public Color.Intensity getColorIntensity() {
        return colorIntensity.get();
    }

    public ObjectProperty<Color.Intensity> colorIntensityProperty() {
        return colorIntensity;
    }

    public void setColorIntensity(final Color.Intensity colorIntensity) {
        this.colorIntensity.set(colorIntensity);
    }

}
