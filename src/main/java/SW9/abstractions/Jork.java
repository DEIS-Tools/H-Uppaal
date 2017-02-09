package SW9.abstractions;

import SW9.code_analysis.Nearable;
import SW9.utility.helpers.LocationAware;
import SW9.utility.serialize.Serializable;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.beans.property.*;

import java.util.concurrent.atomic.AtomicInteger;

public class Jork implements Serializable, Nearable, LocationAware {

    private static final String X = "x";
    private static final String Y = "y";
    private static final String ID = "id";
    private static final String TYPE = "type";
    private static AtomicInteger idGenerator = new AtomicInteger();

    // Modeling properties
    private final StringProperty id = new SimpleStringProperty("");
    private final ObjectProperty<Type> type = new SimpleObjectProperty<>(Type.JOIN);

    // Styling properties
    private final DoubleProperty x = new SimpleDoubleProperty(0d);
    private final DoubleProperty y = new SimpleDoubleProperty(0d);

    public Jork(final Type type) {
        setType(type);

        if (type.equals(Type.JOIN)) {
            setId("J" + idGenerator.incrementAndGet());
        } else {
            setId("F" + idGenerator.incrementAndGet());
        }
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

    public ObjectProperty<Type> typeProperty() {
        return type;
    }

    public Type getType() {
        return type.get();
    }

    public void setType(final Type type) {
        this.type.set(type);
    }

    @Override
    public JsonObject serialize() {
        final JsonObject result = new JsonObject();

        result.addProperty(X, getX());
        result.addProperty(Y, getY());
        result.addProperty(ID, getId());
        result.add(TYPE, new Gson().toJsonTree(getType(), Type.class));

        return result;
    }

    @Override
    public void deserialize(final JsonObject json) {
        setX(json.getAsJsonPrimitive(X).getAsDouble());
        setY(json.getAsJsonPrimitive(Y).getAsDouble());
        setId(json.getAsJsonPrimitive(ID).getAsString());
        setType(new Gson().fromJson(json.getAsJsonPrimitive(TYPE), Type.class));
    }

    @Override
    public String generateNearString() {
        String result = "";

        if (getType().equals(Type.FORK)) {
            result += "Fork ";
        } else {
            result += "Join ";
        }

        result += getId();

        return result;
    }

    public enum Type {
        JOIN,
        FORK
    }
}
