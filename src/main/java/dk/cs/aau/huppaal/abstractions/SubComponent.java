package dk.cs.aau.huppaal.abstractions;

import dk.cs.aau.huppaal.HUPPAAL;
import dk.cs.aau.huppaal.code_analysis.Nearable;
import dk.cs.aau.huppaal.utility.helpers.Circular;
import dk.cs.aau.huppaal.utility.serialize.Serializable;
import com.google.gson.JsonObject;
import javafx.beans.property.*;

public class SubComponent implements Serializable, Circular, Nearable {

    private static final String COMPONENT = "component";
    private static final String IDENTIFIER = "identifier";

    private static final String X = "x";
    private static final String Y = "y";
    private static final String WIDTH = "width";
    private static final String HEIGHT = "height";

    public final BooleanProperty selected = new SimpleBooleanProperty(false);

    // Verification properties
    private final ObjectProperty<Component> component = new SimpleObjectProperty<>(null);
    private final StringProperty identifier = new SimpleStringProperty("");

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);
    private final DoubleProperty width = new SimpleDoubleProperty(200d);
    private final DoubleProperty height = new SimpleDoubleProperty(200d);
    private final Component parent;

    public SubComponent(final Component component, Component parent) {
        setComponent(component);
        this.parent = parent;
        setIdentifier("S" + hashCode());
    }

    public SubComponent(final JsonObject object, Component parent) {
        deserialize(object);
        this.parent = parent;
    }

    public Component getComponent() {
        return component.get();
    }

    public void setComponent(final Component component) {
        this.component.set(component);
    }

    public ObjectProperty<Component> componentProperty() {
        return component;
    }

    public String getIdentifier() {
        return identifier.get();
    }

    public void setIdentifier(final String identifier) {
        this.identifier.set(identifier);
    }

    public StringProperty identifierProperty() {
        return identifier;
    }

    public double getX() {
        return x.get();
    }

    public void setX(double x) {
        this.x.set(x);
    }

    public DoubleProperty xProperty() {
        return x;
    }

    public double getY() {
        return y.get();
    }

    public void setY(double y) {
        this.y.set(y);
    }

    public DoubleProperty yProperty() {
        return y;
    }

    public double getWidth() {
        return width.get();
    }

    public void setWidth(double width) {
        this.width.set(width);
    }

    public DoubleProperty widthProperty() {
        return width;
    }

    public double getHeight() {
        return height.get();
    }

    public void setHeight(double height) {
        this.height.set(height);
    }

    public DoubleProperty heightProperty() {
        return height;
    }

    @Override
    public JsonObject serialize() {
        if(getComponent() == null)
            return null;
        var result = new JsonObject();

        result.addProperty(COMPONENT, getComponent().getName());
        result.addProperty(IDENTIFIER, getIdentifier());

        result.addProperty(X, getX());
        result.addProperty(Y, getY());
        result.addProperty(WIDTH, getWidth());
        result.addProperty(HEIGHT, getHeight());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        HUPPAAL.getProject().getComponents().forEach(c -> {
            if(json.getAsJsonPrimitive(COMPONENT).getAsString().equals(c.getName())) {
                this.component.set(c);
            }
        });

        setIdentifier(json.getAsJsonPrimitive(IDENTIFIER).getAsString());

        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
        setWidth(json.getAsJsonPrimitive(WIDTH).getAsDouble());
        setHeight(json.getAsJsonPrimitive(HEIGHT).getAsDouble());
    }

    @Override
    public DoubleProperty radiusProperty() {
        return new SimpleDoubleProperty(10);
    }

    @Override
    public DoubleProperty scaleProperty() {
        return new SimpleDoubleProperty(10);
    }

    @Override
    public String generateNearString() {
        return "[%s](subcomponent:%s/%s)".formatted(getIdentifier(), parent.getName(), getIdentifier());
    }
}
