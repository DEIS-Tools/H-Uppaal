package SW9.abstractions;

import SW9.utility.serialize.Serializable;
import com.google.gson.JsonObject;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.concurrent.atomic.AtomicInteger;

public class Jork implements Serializable {

    private static final String X = "x";
    private static final String Y = "y";
    private static final String ID = "id";

    private static AtomicInteger idGenerator = new AtomicInteger();

    // Modeling properties
    private final StringProperty id = new SimpleStringProperty("");

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);

    public Jork() {
        setId("J" + idGenerator.incrementAndGet());
    }

    public Jork(final JsonObject jsonObject) {
        deserialize(jsonObject);
    }

    public String getId() {
        return id.get();
    }

    public void setId(final String id) {
        this.id.set(id);
    }

    public StringProperty idProperty() {
        return id;
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

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(X, getX());
        result.addProperty(Y, getY());
        result.addProperty(ID, getId());

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
        setId(json.getAsJsonPrimitive(ID).getAsString());

    }
}
